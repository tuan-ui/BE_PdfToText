package com.shop.Service.updateDoc;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UpdateDocService {
    private final ITesseract ocrEngine;

    public String updateDoc() {
        String inputFilePath = "D:/pdf/input.docx";   // Đường dẫn đến file Word gốc
        String outputFilePath = "D:/pdf/output.docx"; // Đường dẫn lưu file Word sau khi chỉnh sửa
        String[][] rowData = {
                {"1","TCTD 1-1","Dư nợ 1-2", "Mục đích vay vốn 1-3", "Lãi suất 1-4", "Nhóm nợ 1-5", "Ngày đến hạn 1-6"},
                {"2","TCTD 2-1","Dư nợ 2-2", "Mục đích vay vốn 2-3", "Lãi suất 2-4", "Nhóm nợ 2-5", "Ngày đến hạn 2-6"},
                {"3","TCTD 3-1","Dư nợ 3-2", "Mục đích vay vốn 3-3", "Lãi suất 3-4", "Nhóm nợ 3-5", "Ngày đến hạn 3-6"},
        };
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            if (!document.getTables().isEmpty()) {
                XWPFTable table = document.getTables().get(3); // Lấy bảng thứ 4 (index bắt đầu từ 0)

                //  Thiết lập đường viền cho bảng
                table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000"); // Viền ngang bên trong
                table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000"); // Viền dọc bên trong
                table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");

                int numCols = table.getRow(0).getTableCells().size(); // Số cột trong bảng
                // Xác định hàng bắt đầu ghi dữ liệu (hàng thứ 2 - index 1)
                int startRowIndex = 1;

                for (int i = 0; i < rowData.length; i++) {
                    XWPFTableRow row;

                    // Nếu hàng đã tồn tại -> sử dụng lại, nếu không -> tạo mới
                    if (table.getNumberOfRows() > startRowIndex + i) {
                        row = table.getRow(startRowIndex + i);
                    } else {
                        row = table.createRow();
                    }

                    for (int col = 0; col < numCols; col++) {
                        XWPFTableCell cell = row.getCell(col) != null ? row.getCell(col) : row.addNewTableCell();

                        // Xóa nội dung cũ (nếu có)
                        cell.removeParagraph(0);

                        // Thêm đoạn văn và dữ liệu
                        XWPFParagraph paragraph = cell.addParagraph();
                        paragraph.setAlignment(ParagraphAlignment.CENTER); // Căn giữa
                        XWPFRun run = paragraph.createRun();
                        run.setText(rowData[i][col]);
                        run.setBold(true);
                        run.setFontSize(12);

                        // Thêm viền cho ô
                        CTTcBorders borders = cell.getCTTc().addNewTcPr().addNewTcBorders();
                        borders.addNewTop().setVal(STBorder.SINGLE);
                        borders.addNewBottom().setVal(STBorder.SINGLE);
                        borders.addNewLeft().setVal(STBorder.SINGLE);
                        borders.addNewRight().setVal(STBorder.SINGLE);
                    }
                }
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    document.write(fos);
                }
            } else {
                System.out.println(" Không tìm thấy bảng nào trong tài liệu!");
            }

        } catch (IOException ex) {
            throw new RuntimeException(" Lỗi khi xử lý file Word: " + ex.getMessage(), ex);
        }
        return "Thành công";
    }

    public String addColumnToTable() {
        String inputFilePath = "D:/pdf/input.docx";
        String outputFilePath = "D:/pdf/output1.docx";

        try (FileInputStream fis = new FileInputStream(inputFilePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            if (!document.getTables().isEmpty() && document.getTables().size() > 3) {
                XWPFTable table = document.getTables().get(3); // Lấy bảng thứ 4

                //  Thiết lập bảng tự điều chỉnh theo chiều rộng trang
                if (table.getCTTbl().getTblPr() == null) {
                    table.getCTTbl().addNewTblPr();
                }
                if (table.getCTTbl().getTblPr().getTblLayout() == null) {
                    table.getCTTbl().getTblPr().addNewTblLayout().setType(STTblLayoutType.AUTOFIT);
                }

                int rowIndex = 0;
                for (XWPFTableRow row : table.getRows()) {
                    XWPFTableCell newCell = row.addNewTableCell(); // Thêm cột (ô) mới

                    //  Đặt nội dung và định dạng cho ô mới
                    XWPFParagraph paragraph = newCell.addParagraph();
                    newCell.removeParagraph(0);
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun run = paragraph.createRun();
                    // Xóa nội dung cũ (nếu có)

                    run.setText(rowIndex == 0 ? "Tiêu đề mới" : ""); // Tiêu đề cho hàng đầu, dữ liệu cho các hàng sau
                    run.setBold(rowIndex == 0);  // In đậm cho tiêu đề
                    run.setFontSize(12);

                    //  Thêm viền cho ô
                    CTTcBorders borders = newCell.getCTTc().addNewTcPr().addNewTcBorders();
                    borders.addNewTop().setVal(STBorder.SINGLE);
                    borders.addNewBottom().setVal(STBorder.SINGLE);
                    borders.addNewLeft().setVal(STBorder.SINGLE);
                    borders.addNewRight().setVal(STBorder.SINGLE);

                    rowIndex++;
                }

                //  Ghi lại file sau khi chỉnh sửa
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    document.write(fos);
                }

                System.out.println(" Đã thêm 1 cột mới vào bảng!");
            } else {
                System.out.println(" Không tìm thấy bảng thứ 4!");
            }

        } catch (IOException ex) {
            throw new RuntimeException(" Lỗi khi xử lý file Word: " + ex.getMessage(), ex);
        }

        return "Thành công";
    }


    public UpdateDocService() {
        this.ocrEngine = new Tesseract();
        this.ocrEngine.setDatapath("D:/PdfToText/BE/tesseract/tessdata"); // Cập nhật đường dẫn thực tế đến thư mục tessdata
        this.ocrEngine.setLanguage("eng+vie");
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        return removeNoise(adjustBrightnessContrast(applySharpen(convertToGrayscale(image)), 1.3f, 15));
    }

    private BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayscale;
    }

    private BufferedImage applySharpen(BufferedImage image) {
        float[] matrix = { 0, -1, 0, -1, 5, -1, 0, -1, 0 };
        return new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null).filter(image, null);
    }

    private BufferedImage adjustBrightnessContrast(BufferedImage image, float contrast, int brightness) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                int r = clamp((int) (color.getRed() * contrast + brightness));
                int g = clamp((int) (color.getGreen() * contrast + brightness));
                int b = clamp((int) (color.getBlue() * contrast + brightness));
                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return output;
    }

    private BufferedImage removeNoise(BufferedImage image) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                Color color = new Color(image.getRGB(x, y));
                output.setRGB(x, y, color.getRed() < 25 ? Color.WHITE.getRGB() : image.getRGB(x, y));
            }
        }
        return output;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
    public Map<String, String> extractIDCardInfo(MultipartFile file) throws IOException, TesseractException {
        BufferedImage image = preprocessImage(ImageIO.read(file.getInputStream()));
        String text = ocrEngine.doOCR(image);

        Map<String, String> info = new HashMap<>();
        info.put("name", extractWithRegex(text, "(?i)(?:Họ và tên|Full name)[^a-zA-ZÀ-ỹ]*([A-ZÀ-Ỹ][A-Za-zÀ-ỹ\\s]*)"));
        info.put("dob", extractWithRegex(text, "(?i)(?:Ngày sinh|Date of birth|DOB)[\\s.:/-]*((?:\\d{2}/\\d{2}/\\d{4})|(?:\\d{2}-\\d{2}-\\d{4}))"));
        info.put("gender", extractWithRegex(text, "(?i)(?:Giới tính|Sex)[\\s.:/-]*(Nam|Nữ)"));
        info.put("nationality", extractWithRegex(text, "(?i)(?:Quốc tịch|Nationality)[\\s.:/-]*([A-Za-zÀ-ỹ\\s]+)"));
        info.put("idNumber", extractWithRegex(text, "(?i)(?:Số|No)[\\s.:/-]*(\\d{9,12})"));
        info.put("address", extractWithRegex(text, "(?i)(Địa chỉ|Place of ongin):\\s*(.+)"));
        info.put("fullText",text);
        return info;
    }
    private String extractWithRegex(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(matcher.groupCount()) : "";
    }
}
