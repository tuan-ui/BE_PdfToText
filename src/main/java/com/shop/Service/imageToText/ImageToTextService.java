package com.shop.Service.imageToText;

import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageToTextService {

    private static final String TESSDATA_PATH = "D:/PdfToText/BE/tesseract/tessdata";
    //private static final String TESSDATA_PATH = System.getProperty("user.dir") + "/tesseract/tessdata";
    private static final String LANGUAGE = "eng+vie";

    public List<String> pdfToText(List<MultipartFile> files, String search) {

        // kiểm tra search rỗng
        List<String> results = new ArrayList<>();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(LANGUAGE);

        for (MultipartFile file : files) {
            try {
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
                if(extractedText.toLowerCase().contains(search.toLowerCase()))
                    results.add(docxFileName);
            } catch (Exception e) {
                results.add("Lỗi khi xử lý file: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }
        return results;
    }

    private String extractTextFromPDF(MultipartFile pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private String saveTextToDocx(String fileName, String text) throws IOException {
        //Vị trí xuất và đổi đuôi file upload thành .docx
        String docxFileName = "D:/pdf/" + fileName.replaceAll("\\.(png|jpg|jpeg|tiff|pdf)$", ".docx");
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
}