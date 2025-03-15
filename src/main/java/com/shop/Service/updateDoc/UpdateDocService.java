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
import java.io.FileNotFoundException;
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

    public String updateText() throws IOException {
        String inputFilePath = "D:/pdf/input.docx";
        String outputFilePath = "D:/pdf/output1.docx";

        FileInputStream fis = new FileInputStream(inputFilePath);
        XWPFDocument document = new XWPFDocument(fis);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("«hoten_full»", "Nguyễn Văn A");
        replacements.put("«ngaythang»", "01/01/2023");
        replacements.put("«thanhphancbqtd»", "Nguyễn Văn B, Trần Thị C");
        replacements.put("«chucvucbqtd»", "Cán bộ tín dụng");
        replacements.put("«hoten1»", "Nguyễn Văn A");
        replacements.put("«hoten2»", "Trần Thị B");
        replacements.put("«ngaysinh1»", "01/01/1990");
        replacements.put("«ngaysinh2»", "02/02/1991");
        replacements.put("«cmnd1»", "123456789");
        replacements.put("«cmnd2»", "987654321");
        replacements.put("«cccd1»", "112233445566");
        replacements.put("«cccd2»", "665544332211");
        replacements.put("«gioitinh1»", "Nam");
        replacements.put("«gioitinh2»", "Nữ");
        replacements.put("«quanhe1»", "Vợ");
        replacements.put("«quanhe2»", "Chồng");
        replacements.put("«tthonnhan1»", "Đã kết hôn");
        replacements.put("«tthonnhan2»", "Đã kết hôn");
        replacements.put("«diachi1»", "123 Đường ABC, TP. Bảo Lộc");
        replacements.put("«phone1»", "0123456789");
        replacements.put("«phone2»", "0987654321");
        replacements.put("«hocvan1»", "Đại học");
        replacements.put("«hocvan2»", "Cao đẳng");
        replacements.put("«nghenghiep1»", "Kỹ sư");
        replacements.put("«nghenghiep2»", "Giáo viên");
        replacements.put("«songuoinuoi»", "2");
        replacements.put("«nlplds»", "Có");
        replacements.put("«nhanthan»", "Tốt");
        replacements.put("«lsvay»", "Không có nợ xấu");
        replacements.put("«cohaykhong_doituonghc»", "Không");
        replacements.put("«vtc_quy»", "1.000.000.000");
        replacements.put("«nxthongtinCIC_moiqh»", "Không có thông tin");
        replacements.put("«Mucdichvv»", "Mua nhà");
        replacements.put("«Doituongvv»", "Nhà ở");
        replacements.put("«sxkd_diadiem»", "123 Đường XYZ, TP. Bảo Lộc");
        replacements.put("«sxkd_tinhhinh»", "Ổn định");
        replacements.put("«NguoncungcapNL»", "Nhà cung cấp địa phương");
        replacements.put("«ThitruongTTSP»", "Địa phương");
        replacements.put("«KhanangSXKD»", "Tốt");
        replacements.put("«sxkd_tongsold»", "10");
        replacements.put("«sxkd_ldtx»", "8");
        replacements.put("«sxkd_ldktx»", "2");
        replacements.put("«sxkd_dtcm»", "Có");
        replacements.put("«sxkd_khkt»", "Có");
        replacements.put("«Hieuquaxahoi»", "Tích cực");
        replacements.put("«Mota_dgts»", "Nhà ở mặt tiền");
        replacements.put("«tonggtts»", "2.000.000.000");
        replacements.put("«tylecvsogtts»", "50");
        replacements.put("«vonvay»", "1.000.000.000");
        replacements.put("«stvaybangchu»", "Một tỷ đồng");
        replacements.put("«tlcv_thsonvnh»", "30");
        replacements.put("«thoihan»", "12 tháng");
        replacements.put("«laisuat»", "10");
        replacements.put("«ptvay»", "Trả góp");
        replacements.put("«Khtrano»", "Hàng tháng");
        replacements.put("«phanquyet»", "Đồng ý cho vay");

        // Thay thế trong các đoạn văn bản
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    for (Map.Entry<String, String> entry : replacements.entrySet()) {
                        if (text.contains(entry.getKey())) {
                            text = text.replace(entry.getKey(), entry.getValue());
                            run.setText(text, 0);
                        }
                    }
                }
            }
        }

        // Thay thế trong các bảng
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            String text = run.getText(0);
                            if (text != null) {
                                for (Map.Entry<String, String> entry : replacements.entrySet()) {
                                    if (text.contains(entry.getKey())) {
                                        text = text.replace(entry.getKey(), entry.getValue());
                                        run.setText(text, 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lưu tài liệu
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        document.write(fos);
        fos.close();
        document.close();
        fis.close();

        return "Thành công";
    }
    public String updateTable() throws IOException {
        String inputFilePath = "D:/pdf/input.docx";
        String outputFilePath = "D:/pdf/output2.docx";

        FileInputStream fis = new FileInputStream(inputFilePath);
        XWPFDocument document = new XWPFDocument(fis);

        String searchString = "«nxthongtinCIC_quy»";

        // Duyệt qua các đoạn văn bản trong tài liệu
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String paragraphText = paragraph.getText();

            // Kiểm tra nếu đoạn chứa chuỗi cần thay thế
            if (paragraphText != null && paragraphText.contains(searchString)) {
                // Xóa đoạn văn bản chứa chuỗi
                document.removeBodyElement(document.getPosOfParagraph(paragraph));

                // Chèn bảng vào vị trí của đoạn văn bản đã xóa
                XWPFTable table = document.insertNewTbl(document.getParagraphs().get(i).getCTP().newCursor());

                // Tạo hàng và ô trong bảng
                XWPFTableRow row1 = table.getRow(0);
                if (row1 == null) {
                    row1 = table.createRow();
                }
                row1.getCell(0).setText("STT");
                row1.addNewTableCell().setText("TCTD");
                row1.addNewTableCell().setText("Dư nợ");
                row1.addNewTableCell().setText("Mục đích vay vốn");
                row1.addNewTableCell().setText("Lãi suất");
                row1.addNewTableCell().setText("Nhóm nợ");
                row1.addNewTableCell().setText("Ngày đến hạn");

                // Thêm dữ liệu vào bảng (ví dụ)
                XWPFTableRow row2 = table.createRow();
                row2.getCell(0).setText("1");
                row2.getCell(1).setText("Ngân hàng A");
                row2.getCell(2).setText("500.000.000");
                row2.getCell(3).setText("Mua nhà");
                row2.getCell(4).setText("10%");
                row2.getCell(5).setText("Nhóm 1");
                row2.getCell(6).setText("01/01/2024");

                XWPFTableRow row3 = table.createRow();
                row3.getCell(0).setText("2");
                row3.getCell(1).setText("Ngân hàng B");
                row3.getCell(2).setText("300.000.000");
                row3.getCell(3).setText("Kinh doanh");
                row3.getCell(4).setText("12%");
                row3.getCell(5).setText("Nhóm 2");
                row3.getCell(6).setText("01/06/2024");

                break; // Thoát khỏi vòng lặp sau khi thay thế
            }
        }

        // Lưu tài liệu
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        document.write(fos);
        fos.close();
        document.close();
        fis.close();

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
