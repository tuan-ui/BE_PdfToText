package com.shop.Utils.PdfToWordConverter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;

public class PdfToWordConverter {
    public static void convertPdfToWord(File pdfFile, File wordFile) throws IOException {
        // Đọc nội dung từ PDF
        String extractedText;
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractedText = pdfStripper.getText(document);
        }

        // Ghi nội dung vào file Word
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(wordFile)) {

            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(extractedText);
            run.setFontSize(12);
            doc.write(out);
        }
    }

    public static void main(String[] args) throws IOException {
        File pdfFile = new File("input.pdf");   // Đường dẫn file PDF
        File wordFile = new File("output.docx"); // Đường dẫn file Word xuất ra

        convertPdfToWord(pdfFile, wordFile);
        System.out.println("✅ Chuyển đổi thành công: " + wordFile.getAbsolutePath());
    }
}
