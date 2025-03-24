package com.shop.Controller.Spacy;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class SpacyApiController {

    private static final String PYTHON_API_URL = "http://127.0.0.1:5001/process";

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processText(@RequestParam("file") MultipartFile file, @RequestParam("request") String request) {
        RestTemplate restTemplate = new RestTemplate();

        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

        // Thiết lập headers với boundary
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/form-data; boundary=" + boundary));

        // Tạo body với file và request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(convertMultipartFileToFile(file)));
        body.add("request", request);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Gửi yêu cầu POST đến FastAPI
        ResponseEntity<String> response = restTemplate.exchange(PYTHON_API_URL, HttpMethod.POST, requestEntity, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    // Hàm chuyển đổi MultipartFile thành File
    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        try {
            File file = File.createTempFile("temp", null);
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error converting MultipartFile to File", e);
        }
    }
}



