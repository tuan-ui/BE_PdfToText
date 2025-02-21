package com.shop.Service.updateDoc;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class UpdateDocService {
    public String updateDoc() {
        String inputFilePath = "D:/pdf/input.docx";   // Đường dẫn đến file Word gốc
        String outputFilePath = "D:/pdf/output.docx"; // Đường dẫn lưu file Word sau khi chỉnh sửa

        try (FileInputStream fis = new FileInputStream(inputFilePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            if (!document.getTables().isEmpty()) {
                XWPFTable table = document.getTables().get(3); // Lấy bảng thứ 4 (index bắt đầu từ 0)

                // ✅ Thiết lập đường viền cho bảng
                table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000"); // Viền ngang bên trong
                table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000"); // Viền dọc bên trong
                table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
                table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");

                int numCols = table.getRow(0).getTableCells().size(); // Số cột
                int numRowsToAdd = 3;  // Số dòng trống cần thêm

                // ✅ Thêm 3 dòng trống với border cho từng ô
                for (int i = 0; i < numRowsToAdd; i++) {
                    XWPFTableRow emptyRow = table.createRow();
                    for (int col = 0; col < numCols; col++) {
                        if (emptyRow.getCell(col) == null) {
                            emptyRow.addNewTableCell(); // Thêm ô nếu chưa có
                        }
                        XWPFTableCell cell = emptyRow.getCell(col);
                        cell.setText(""); // Đặt ô trống
                        //  Thêm viền cho từng ô
                        CTTcBorders borders = cell.getCTTc().addNewTcPr().addNewTcBorders();
                        borders.addNewTop().setVal(STBorder.SINGLE);
                        borders.addNewBottom().setVal(STBorder.SINGLE);
                        borders.addNewLeft().setVal(STBorder.SINGLE);
                        borders.addNewRight().setVal(STBorder.SINGLE);


                    }
                }

                // ✅ Ghi lại file sau khi chỉnh sửa
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    document.write(fos);
                }

                System.out.println("✅ Đã thêm 3 dòng trống và viền cho bảng trong file Word!");
            } else {
                System.out.println("⚠️ Không tìm thấy bảng nào trong tài liệu!");
            }

        } catch (IOException ex) {
            throw new RuntimeException("❌ Lỗi khi xử lý file Word: " + ex.getMessage(), ex);
        }
        return "Thành công";
    }
}
