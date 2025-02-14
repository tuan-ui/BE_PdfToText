package com.shop.Controller.ImageToText;

import com.shop.Service.imageToText.ImageToTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:5173")
public class ImageToTextController {

    @Autowired
    private ImageToTextService imageToTextService;

    @PostMapping("/PdfToText")
    public ResponseEntity<Map<String, String>> processFiles(@RequestParam("files") List<MultipartFile> files,
                                                            @RequestParam("search") String search) {
        Map<String, String> response = new HashMap<>();
        List<String> result = imageToTextService.pdfToText(files,search);
        response.put("status", "success");
        response.put("message", "Thanh cong");

        return ResponseEntity.ok(response);
    }
}

