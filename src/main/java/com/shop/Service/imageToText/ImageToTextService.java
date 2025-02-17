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
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageToTextService {

    @Autowired
    private DocxRepository docxRepository;
    private static final String TESSDATA_PATH = "D:/PdfToText/BE/tesseract/tessdata";
    //private static final String TESSDATA_PATH = System.getProperty("user.dir") + "/tesseract/tessdata";
    private static final String LANGUAGE = "eng+vie";
    private static final String FOLDER_PATH = "D:/pdf";

    public ImportResponseDTO pdfToText(List<MultipartFile> files) {
        ImportResponseDTO response = new ImportResponseDTO();
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setStatusValue("OK");
        response.setExecuteDate(LocalDateTime.now());
        List<String> results = new ArrayList<>();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(LANGUAGE);

        for (MultipartFile file : files) {
            try {
                DocxText docxText = new DocxText();
                String extractedText;
                String fileName = file.getOriginalFilename();
                if (isImageFile(fileName)) {
                    BufferedImage image = ImageIO.read(file.getInputStream());
                    extractedText = tesseract.doOCR(image);
                } else if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                    extractedText = extractTextFromPDF(file);
                } else {
                    results.add("Định dạng file không được hỗ trợ: " + fileName);
                    continue;
                }
                String docxFileName = saveTextToDocx(fileName, extractedText);
                docxText.setFileName(fileName);
                docxText.setDescription(ClobConverter.createClob(extractedText));
                docxText.setPath(docxFileName);
                docxText.setCreateDate(LocalDateTime.now());
                docxRepository.save(docxText);
            } catch (Exception e) {
                results.add("Lỗi khi xử lý file: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }
        results.add("Import hoàn tất");
        response.setMessage(results);
        return response;
    }

    private String extractTextFromPDF(MultipartFile pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document); // Lấy text nếu có

            // Dùng PDFRenderer để trích xuất ảnh từ PDF
            //Lỗi khi scan file hình quá nhỏ -> Điều chỉnh dpi
            //Line cannot be recognized!!
            //Image too small to scale!! (1x36 vs min width of 3)
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSDATA_PATH);
            tesseract.setLanguage(LANGUAGE);

            StringBuilder ocrText = new StringBuilder(extractedText);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String ocrResult = tesseract.doOCR(image);
                ocrText.append("\n").append(ocrResult);
            }

            return ocrText.toString();
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }

    private String saveTextToDocx(String fileName, String text) throws IOException {
        //Vị trí xuất và đổi đuôi file upload thành .docx
        String docxFileName = FOLDER_PATH + "/" + fileName.replaceAll("\\.(png|jpg|jpeg|tiff|pdf)$", ".docx");
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(docxFileName)) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            document.write(out);
        }
        return docxFileName;
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".tiff");
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
}