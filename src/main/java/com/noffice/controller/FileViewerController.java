package com.noffice.controller;

import com.noffice.entity.DocumentFiles;
import com.noffice.entity.User;
import com.noffice.repository.DocumentFileRepository;
import com.noffice.ultils.AppConfig;
import com.noffice.ultils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fileViewer")
public class FileViewerController {

    private final DocumentFileRepository documentFileRepository;
    @Value("${URL.COLLABORA.OFFICE}")
    private String urlCollaboraOffice;

    @Value("${API.URL}")
    private String apiUrl;
    @Value("${save_path}")
    private String savePath;

    public FileViewerController(DocumentFileRepository documentFileRepository) {
        this.documentFileRepository = documentFileRepository;
    }

    @GetMapping("/wopiUrl/{filename}")
    public ResponseEntity<?> getWopiUrl(@PathVariable String filename) {
        Map<String, Object> res = new HashMap<>();
        String wopiSrc = apiUrl+ "/wopi/files/" + filename;
        String encodedWopiSrc = URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);
        String url = urlCollaboraOffice+"/browser/dist/cool.html?WOPISrc=" + encodedWopiSrc;
        res.put("Name", filename);
        res.put("Url", url);
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            Path uploadPath = Paths.get(savePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = System.nanoTime() +"_"+ file.getOriginalFilename();

            // Lưu file vào thư mục
            Path filePath = uploadPath.resolve(originalFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String wopiSrc = apiUrl+ "/wopi/files/" + originalFilename;
            String encodedWopiSrc = URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);
            String url = urlCollaboraOffice+"/browser/dist/cool.html?WOPISrc=" + encodedWopiSrc;
            DocumentFiles documentFiles = new DocumentFiles();
            documentFiles.setAttachName(originalFilename);
            documentFiles.setAttachPath(url);
            documentFiles.setPartnerId(userDetails.getPartnerId());
            documentFiles.setCreateBy(userDetails.getId());
            DocumentFiles saved = documentFileRepository.save(documentFiles);
            res.put("id", saved.getId());
            res.put("url", url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        res.put("message","success");
        res.put("status",200);
        return ResponseEntity.ok(res);
    }
}
