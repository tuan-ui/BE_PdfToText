package com.shop.Service.imageToText;

import com.shop.Utils.ClobConverter;
import com.shop.Utils.PageDTO;
import com.shop.Dto.DocxTextResponseDTO;
import com.shop.Dto.ImportResponseDTO;
import com.shop.Entity.docxText.DocxText;
import com.shop.Repository.docText.DocxRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageToTextService {

    @Autowired
    private DocxRepository docxRepository;

    private static final String TESSDATA_PATH = "D:/PdfToText/BE/tesseract/tessdata";
    private static final String LANGUAGE = "eng+vie";
    private static final String FOLDER_PATH = "D:/pdf";
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public ImportResponseDTO pdfToText(List<MultipartFile> files) {
        ImportResponseDTO response = new ImportResponseDTO();
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setStatusValue("OK");
        response.setExecuteDate(LocalDateTime.now());

        List<String> results = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (MultipartFile file : files) {
            executor.submit(() -> processFile(file, results));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                results.add("Quá trình xử lý bị dừng do hết thời gian.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            results.add("Quá trình xử lý bị gián đoạn: " + e.getMessage());
        }

        results.add("Import hoàn tất");
        response.setMessage(results);
        return response;
    }

    private void processFile(MultipartFile file, List<String> results) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                results.add("Tên file không hợp lệ.");
                return;
            }

            String extractedText;
            if (isImageFile(fileName)) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                BufferedImage processedImage = preprocessImage(image);
                extractedText = doOCR(processedImage);
            } else if (fileName.toLowerCase().endsWith(".pdf")) {
                extractedText = extractTextFromPDF(file);
            } else {
                results.add("Định dạng file không được hỗ trợ: " + fileName);
                return;
            }

            String docxFileName = saveTextToDocx(fileName, extractedText);
            DocxText docxText = new DocxText();
            docxText.setFileName(fileName);
            docxText.setDescription(ClobConverter.createClob(extractedText));
            docxText.setPath(docxFileName);
            docxText.setCreateDate(LocalDateTime.now());
            docxRepository.save(docxText);

        } catch (Exception e) {
            results.add("Lỗi khi xử lý file: " + file.getOriginalFilename() + " - " + e.getMessage());
        }
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // 🔍 Sharpening kernel
        float[] sharpenKernel = {
                0.0f, -1.0f, 0.0f,
                -1.0f, 5.0f, -1.0f,
                0.0f, -1.0f, 0.0f
        };
        BufferedImageOp sharpenOp = new ConvolveOp(new Kernel(3, 3, sharpenKernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage sharpenedImage = sharpenOp.filter(grayImage, null);

        // 💡 Brightness/contrast adjustment
        RescaleOp rescaleOp = new RescaleOp(1.2f, 15, null); // Increase brightness and contrast
        BufferedImage adjustedImage = rescaleOp.filter(sharpenedImage, null);

        // 🧹 Noise removal (simple median filter approximation)

        return new MedianFilter().filter(adjustedImage,null);
    }

    private String doOCR(BufferedImage image) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(LANGUAGE);
        return tesseract.doOCR(image);
    }

    private String extractTextFromPDF(MultipartFile pdfFile) throws IOException, TesseractException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            StringBuilder ocrText = new StringBuilder(new PDFTextStripper().getText(document));
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                BufferedImage processedImage = preprocessImage(image);
                ocrText.append("\n").append(doOCR(processedImage));
            }

            return ocrText.toString();
        }
    }

    private String saveTextToDocx(String fileName, String text) throws IOException {
        String docxFileName = FOLDER_PATH + "/" + fileName.replaceAll("\\.(png|jpg|jpeg|tiff|pdf)$", ".docx");
        try (XWPFDocument document = new XWPFDocument(); FileOutputStream out = new FileOutputStream(docxFileName)) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText(text);
            document.write(out);
        }
        return docxFileName;
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".tiff");
    }

    // 🧹 Simple median filter (for noise removal)
    private static class MedianFilter implements BufferedImageOp {
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            if (dest == null) {
                dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            }
            for (int y = 1; y < src.getHeight() - 1; y++) {
                for (int x = 1; x < src.getWidth() - 1; x++) {
                    int[] pixels = new int[9];
                    int idx = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            pixels[idx++] = src.getRGB(x + kx, y + ky);
                        }
                    }
                    Arrays.sort(pixels);
                    dest.setRGB(x, y, pixels[4]); // Median pixel
                }
            }
            return dest;
        }

        @Override public Rectangle2D getBounds2D(BufferedImage src) { return src.getRaster().getBounds(); }
        @Override public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
            return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        }
        @Override public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return (Point2D) srcPt.clone(); }
        @Override public RenderingHints getRenderingHints() { return null; }
    }

    public DocxTextResponseDTO searchText(String search, Pageable pageable) {
        DocxTextResponseDTO response = new DocxTextResponseDTO();
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setStatusValue("OK");
        response.setExecuteDate(LocalDateTime.now());

        if (search == null || search.trim().isEmpty()) {
            response.setListDocxText(new ArrayList<>());
            return response;
        }
        List<DocxText> listDocx = docxRepository.findAllDocxText();
        //Dùng phương thức search khác
        List<DocxText> filteredDocx = new ArrayList<>();
        for (DocxText docx : listDocx) {
            try {
                String description = clobToString(docx.getDescription());
                if (description.toLowerCase().contains(search.toLowerCase())) {
                    filteredDocx.add(docx);
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }

        PageDTO pageInfo = new PageDTO();
        pageInfo.setNumber(pageable.getPageNumber());
        pageInfo.setSize(pageable.getPageSize());
        pageInfo.setTotalElements(filteredDocx.size());
        pageInfo.setTotalPages((int) Math.ceil(filteredDocx.size() / (double) pageable.getPageSize()));

        response.setPageInfo(pageInfo);
        response.setListDocxText(filteredDocx);
        return response;
    }

    private String clobToString(Clob clob) throws SQLException, IOException {
        if (clob == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Reader reader = clob.getCharacterStream();
        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public DocxTextResponseDTO getAllDocxText(Pageable pageable) {
        DocxTextResponseDTO response = new DocxTextResponseDTO();
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setStatusValue("OK");
        response.setExecuteDate(LocalDateTime.now());

        Page<DocxText> listDocx = docxRepository.findAllDocxTextPageable(pageable);

        PageDTO pageInfo = new PageDTO();
        pageInfo.setNumber(listDocx.getNumber());
        pageInfo.setSize(listDocx.getSize());
        pageInfo.setTotalElements((int) listDocx.getTotalElements());
        pageInfo.setTotalPages(listDocx.getTotalPages());

        response.setPageInfo(pageInfo);
        response.setListDocxText(listDocx.getContent());
        return response;
    }

    public List<String> extractIdCardInfo(MultipartFile file) throws IOException, TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(LANGUAGE);

        BufferedImage image = ImageIO.read(file.getInputStream());
        image = preprocessImage(image); // Tiền xử lý ảnh
        String rawText = tesseract.doOCR(image);

        return parseIdCardInfo(rawText);
    }

    private List<String> parseIdCardInfo(String text) {
        List<String> result = new ArrayList<>();

        // Regex tìm kiếm các trường
        String name = extractWithRegex(text, "(?i)(Họ và tên|Ten):\\s*(.+)");
        String dob = extractWithRegex(text, "(?i)(Ngày sinh|DOB):\\s*(\\d{2}/\\d{2}/\\d{4})");
        String gender = extractWithRegex(text, "(?i)(Giới tính|Sex):\\s*(Nam|Nữ)");
        String nationality = extractWithRegex(text, "(?i)(Quốc tịch|Nationality):\\s*(\\w+)");
        String idNumber = extractWithRegex(text, "(?i)(Số|No):\\s*(\\d{9,12})");
        String address = extractWithRegex(text, "(?i)(Địa chỉ|Address):\\s*(.+)");
        String issuedDate = extractWithRegex(text, "(?i)(Ngày cấp|Issued):\\s*(\\d{2}/\\d{2}/\\d{4})");

        // Thêm kết quả vào list
        result.add("Họ và tên: " + (name != null ? name : "Không tìm thấy"));
        result.add("Ngày sinh: " + (dob != null ? dob : "Không tìm thấy"));
        result.add("Giới tính: " + (gender != null ? gender : "Không tìm thấy"));
        result.add("Quốc tịch: " + (nationality != null ? nationality : "Không tìm thấy"));
        result.add("Số CMND: " + (idNumber != null ? idNumber : "Không tìm thấy"));
        result.add("Địa chỉ: " + (address != null ? address : "Không tìm thấy"));
        result.add("Ngày cấp: " + (issuedDate != null ? issuedDate : "Không tìm thấy"));

        return result;
    }

    private String extractWithRegex(String text, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        return matcher.find() ? matcher.group(matcher.groupCount()) : null;
    }

}