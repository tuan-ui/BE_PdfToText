package com.shop.Controller.PdfToText;

import com.shop.Service.pdfToText.PdfTextExtractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfTextExtractorService pdfTextExtractorService;

    public PdfController(PdfTextExtractorService pdfTextExtractorService) {
        this.pdfTextExtractorService = pdfTextExtractorService;
    }

    @GetMapping("/extract-folder")
    public ResponseEntity<String> extractTextFromFolder() {
        try {
            String result = pdfTextExtractorService.extractTextFromFolder();
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý file PDF: " + e.getMessage());
        }
    }

    @GetMapping("/convert")
    public String convertAllPdf() {
        pdfTextExtractorService.convertAllPdfInFolder();
        return "Hoàn thành";
    }
}

