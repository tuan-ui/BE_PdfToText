package com.shop.Service.pdfToText;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import org.apache.poi.xwpf.usermodel.*;

import java.io.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class PdfTextExtractorService {

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("pdf");
    private static final String PDF_DIRECTORY = "D:/pdf";

    public String extractTextFromFolder() throws IOException {

        File folder = new File(PDF_DIRECTORY);
        if (!folder.exists() || !folder.isDirectory()) {
            return "Thư mục không tồn tại hoặc không hợp lệ!";
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return "Không tìm thấy file nào trong thư mục!";
        }

        for (File file : files) {
            if (isPdfFile(file)) {
                StringBuilder extractedText = new StringBuilder();
                File outputFile = new File(folder.getPath() + "/"+file.getName()+"_output.txt");
                FileWriter writer = new FileWriter(outputFile);

                String text = extractTextFromPdf(file);
                extractedText.append(text);

                writer.write(extractedText.toString());
                writer.close();
            }
        }

        return "Trích xuất hoàn tất!" ;
    }

    private boolean isPdfFile(File file) {
        return file.isFile() && SUPPORTED_EXTENSIONS.contains(getFileExtension(file));
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1).toLowerCase();
    }

    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    public void convertAllPdfInFolder() {
        File folder = new File(PDF_DIRECTORY);

        // Kiểm tra thư mục có tồn tại không
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println(" Thư mục không tồn tại hoặc không hợp lệ: " + PDF_DIRECTORY);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy file PDF nào trong thư mục.");
            return;
        }

        for (File pdfFile : files) {
            try {
                File wordFile = convertPdfToWord(pdfFile);
                System.out.println("Đã chuyển đổi: " + wordFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠ Lỗi khi xử lý file: " + pdfFile.getName() + " -> " + e.getMessage());
            }
        }
    }

    public File convertPdfToWord(File pdfFile) throws IOException {
        File wordFile = new File(pdfFile.getParent(), pdfFile.getName().replace(".pdf", ".docx"));

        // Đọc văn bản từ PDF
        String extractedText;
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setSuppressDuplicateOverlappingText(true);
            pdfStripper.setAddMoreFormatting(false);
            extractedText = pdfStripper.getText(document);
        }

        // Ghi vào file Word
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(wordFile)) {

            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(extractedText);
            run.setFontSize(12);
            doc.write(out);
        }

        return wordFile;
    }
}

