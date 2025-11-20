package com.noffice.controller;

import com.noffice.entity.DocumentFiles;
import com.noffice.entity.User;
import com.noffice.repository.DocumentAllowedEditorsRepository;
import com.noffice.repository.DocumentAllowedViewersRepository;
import com.noffice.repository.DocumentFileRepository;
import com.noffice.service.JwtService;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fileViewer")
public class FileViewerController {

    private final DocumentFileRepository documentFileRepository;
    @Value("${URL.COLLABORA.OFFICE}")
    private String urlCollaboraOffice;

    @Value("${API.URL}")
    private String apiUrl;
    @Value("${save_path}")
    private String savePath;

    private final DocumentAllowedEditorsRepository editorsRepo;
    private final DocumentAllowedViewersRepository viewersRepo;
    private final JwtService jwtService;

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

    @PostMapping("/createTemp")
    public ResponseEntity<?> createTempFile(@RequestBody UUID fileId) {
        Map<String, Object> res = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        try {
            DocumentFiles originalFile = documentFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // === KIỂM TRA QUYỀN ===
            boolean isCreator = originalFile.getCreateBy().equals(user.getId());
            boolean isEditor = editorsRepo.findByDocumentId(fileId).stream()
                    .anyMatch(e -> e.getEditorId().equals(user.getId()));
            boolean isViewer = viewersRepo.findByDocumentId(fileId).stream()
                    .anyMatch(v -> v.getViewerId().equals(user.getId()));

            boolean canEdit = isCreator || isEditor;
            boolean canView = canEdit || isViewer;

            if (!canView) {
                return ResponseEntity.status(403).body(Map.of("error", "Không có quyền truy cập"));
            }

            // === TÌM FILE ĐANG ĐƯỢC DÙNG ĐỂ EDIT (active editing file) ===
            DocumentFiles activeEditingFile = null;
            String lockValue = null;
            boolean isModified = false;

            // 1. Kiểm tra đã có file temp đang hoạt động là có người đang dùng
            List<DocumentFiles> activeTemps = documentFileRepository.findByOriginalFileIdAndIsTemp(fileId, true);
            for (DocumentFiles temp : activeTemps) {
                if (temp.getLockValue() != null && temp.getLockExpiresAt() != null
                        && temp.getLockExpiresAt().isAfter(LocalDateTime.now())) {
                    activeEditingFile = temp;
                    lockValue = temp.getLockValue();
                    break;
                }
            }

            DocumentFiles fileToOpen;
            UUID fileToOpenId;

            if (canEdit) {
                // === USER CÓ QUYỀN EDIT ===
                if (activeEditingFile != null) {
                    // ĐÃ CÓ NGƯỜI KHÁC ĐANG EDIT → DÙNG CHUNG FILE TEMP ĐÓ → real-time collab
                    fileToOpen = activeEditingFile;
                    fileToOpenId = activeEditingFile.getId();

                    activeEditingFile.setLockExpiresAt(LocalDateTime.now().plusMinutes(30));
                    documentFileRepository.save(activeEditingFile);

                } else {
                    // CHƯA AI MỞ → TẠO MỚI FILE TEMP
                    lockValue = UUID.randomUUID().toString();

                    Path originalPath = Paths.get(savePath).resolve(originalFile.getAttachName());
                    byte[] content = Files.readAllBytes(originalPath);
                    String tempFilename = "temp_" + System.nanoTime() + "_" + originalFile.getAttachName();
                    Path tempPath = Paths.get(savePath).resolve(tempFilename);
                    Files.write(tempPath, content);

                    DocumentFiles newTemp = new DocumentFiles();
                    newTemp.setAttachName(tempFilename);
                    newTemp.setAttachPath(tempPath.toString());
                    newTemp.setOriginalFileId(fileId);
                    newTemp.setCreateBy(user.getId());
                    newTemp.setIsTemp(true);
                    newTemp.setLockValue(lockValue);
                    newTemp.setLockUserId(user.getId());
                    newTemp.setLockExpiresAt(LocalDateTime.now().plusMinutes(30));
                    fileToOpen = documentFileRepository.save(newTemp);
                    fileToOpenId = fileToOpen.getId();
                }

            } else {
                // === CHỈ CÓ QUYỀN XEM ===
                if (activeEditingFile != null) {
                    // Có người đang sửa → mở file temp (live version) ở chế độ view
                    fileToOpen = activeEditingFile;
                    isModified = true; // không cho save
                } else {
                    // Không ai sửa → mở file gốc ở chế độ view
                    fileToOpen = originalFile;
                    isModified = true;
                }
                fileToOpenId = fileToOpen.getId();
                lockValue = null;
            }

            // === TẠO WOPI URL DẪN ĐẾN FILE ĐANG ĐƯỢC DÙNG ===
            String permission = canEdit ? "edit" : "view";
            String wopiToken = jwtService.generateWopiToken(
                    user.getId(),
                    fileId,
                    permission,
                    user.getFullName()
            );

            String filenameForWopi = fileToOpen.getAttachName();
            String wopiSrc = apiUrl + "/wopi/files/" + filenameForWopi + "?access_token=" + wopiToken;
            String encodedWopiSrc = URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);
            String url = urlCollaboraOffice + "/browser/dist/cool.html?WOPISrc=" + encodedWopiSrc;

            res.put("id", fileToOpenId);
            res.put("url", url);
            res.put("lockValue", lockValue);
            res.put("isModified", isModified);
            res.put("collab", canEdit && activeEditingFile != null);

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/deleteTemp/{tempId}")
    public ResponseEntity<?> deleteTemp(@PathVariable UUID tempId) {
        DocumentFiles tempFile = documentFileRepository.findById(tempId).orElse(null);
        if (tempFile != null && Boolean.TRUE.equals(tempFile.getIsTemp())) {
            Path path = Paths.get(savePath).resolve(tempFile.getAttachName());
            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
            documentFileRepository.delete(tempFile);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlockFile(@RequestBody Map<String, Object> payload) {
        UUID fileId = UUID.fromString((String) payload.get("fileId"));
        String lockValue = (String) payload.get("lockValue");

        try {
            DocumentFiles file = documentFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Chỉ unlock nếu lockValue khớp
            if (lockValue != null && lockValue.equals(file.getLockValue())) {
                file.setLockValue(null);
                file.setLockUserId(null);
                file.setLockExpiresAt(null);
                documentFileRepository.save(file);
                return ResponseEntity.ok(Map.of("message", "Unlocked"));
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid lock token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/saveFromTemp")
    public ResponseEntity<?> saveFromTemp(@RequestBody Map<String, Object> payload) {
        UUID tempId = UUID.fromString((String) payload.get("tempId"));
        String lockValue = (String) payload.get("lockValue");

        try {
            DocumentFiles tempFile = documentFileRepository.findById(tempId)
                    .orElseThrow(() -> new RuntimeException("Temp file not found"));

            if (!Boolean.TRUE.equals(tempFile.getIsTemp())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not a temp file"));
            }

            UUID originalId = tempFile.getOriginalFileId();
            if (originalId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No original file"));
            }

            DocumentFiles originalFile = documentFileRepository.findById(originalId)
                    .orElseThrow(() -> new RuntimeException("Original file not found"));

            // Kiểm tra lock hợp lệ
            if (!lockValue.equals(originalFile.getLockValue())) {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid lock"));
            }

            // Copy temp → gốc
            Path tempPath = Paths.get(savePath).resolve(tempFile.getAttachName());
            Path originalPath = Paths.get(savePath).resolve(originalFile.getAttachName());
            Files.copy(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật thông tin
            originalFile.setUpdateBy(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
            originalFile.setUpdateAt(LocalDateTime.now());
            documentFileRepository.save(originalFile);

            return ResponseEntity.ok(Map.of(
                    "message", "Lưu thành công",
                    "isModified", true
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/getCollaboraUrl")
    public ResponseEntity<?> getCollaboraUrl() {
        Map<String, Object> res = new HashMap<>();
        res.put("url", urlCollaboraOffice + "/browser/dist/cool.html");
        return ResponseEntity.ok(res);
    }
}
