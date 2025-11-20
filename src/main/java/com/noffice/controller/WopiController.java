package com.noffice.controller;

import com.noffice.entity.DocumentFiles;
import com.noffice.repository.DocumentAllowedEditorsRepository;
import com.noffice.repository.DocumentFileRepository;
import com.noffice.service.JwtService;
import com.noffice.ultils.AppConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/wopi/files")
@CrossOrigin(
        origins = "${URL.COLLABORA.OFFICE}",
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class WopiController {

    private static final String FILE_DIR = AppConfig.get("save_path");
    private final DocumentFileRepository documentFileRepository;

    public WopiController(DocumentFileRepository documentFileRepository) {
        this.documentFileRepository = documentFileRepository;
    }

    // 1. CheckFileInfo – BẮT BUỘC ĐỂ CHỈNH SỬA
    @Autowired
    private JwtService jwtService;

    @Autowired
    private DocumentAllowedEditorsRepository editorsRepo;

    // 1. CheckFileInfo – BẮT BUỘC ĐỂ CHỈNH SỬA
    @GetMapping("/{filename}")
    public ResponseEntity<?> checkFile(
            @PathVariable String filename,
            @RequestParam("access_token") String token) {  // ← token từ Collabora

        try {
            // 1. GIẢI MÃ TOKEN (không tạo mới!)
            Claims claims = jwtService.extractAllClaims(token);  // Dùng JwtService hiện tại

            UUID userId = UUID.fromString(claims.get("user_id", String.class));
            UUID fileId = UUID.fromString(claims.get("fileId", String.class));
            String mode = claims.get("mode", String.class);
            String fullName = claims.get("fullName", String.class);

            // 2. Tìm file theo tên
            DocumentFiles file = documentFileRepository.findByAttachName(filename);
            if (file == null) {
                return ResponseEntity.status(404).body("File not found");
            }

            // 3. Kiểm tra fileId trong token có khớp với file thực tế không
            if(file.getOriginalFileId() == null)
            {
                if (!file.getId().equals(fileId)) {
                    return ResponseEntity.status(403).body("Token không hợp lệ cho file này");
                }
            }
            else if (!file.getOriginalFileId().equals(fileId)) {
                return ResponseEntity.status(403).body("Token không hợp lệ cho file này");
            }

            // 4. Kiểm tra quyền
            boolean isCreator = file.getCreateBy().equals(userId);
            boolean isEditor = editorsRepo.findByDocumentId(fileId).stream()
                    .anyMatch(e -> e.getEditorId().equals(userId));

            boolean canEdit = isCreator || isEditor;

            if ("edit".equals(mode) && !canEdit) {
                return ResponseEntity.status(403).body("Không có quyền sửa");
            }

            // 5. Kiểm tra isModified (nếu cần)
            if (Boolean.TRUE.equals(file.getIsModified()) && "edit".equals(mode)) {
                return ResponseEntity.status(403).body("File đã bị khóa sửa đổi");
            }

            // 6. Trả về WOPI CheckFileInfo
            Map<String, Object> info = new HashMap<>();
            info.put("BaseFileName", filename);
            info.put("OwnerId", file.getCreateBy().toString());
            info.put("UserId", userId.toString());
            info.put("Size", Files.size(Paths.get(AppConfig.get("save_path"), filename)));
            info.put("Version", String.valueOf(System.currentTimeMillis()));
            info.put("UserCanWrite", "edit".equals(mode));
            info.put("SupportsUpdate", "edit".equals(mode));
            info.put("UserFriendlyName", fullName);
            info.put("PostMessageOrigin", AppConfig.get("URL.COLLABORA.OFFICE"));

            return ResponseEntity.ok()
                    .header("X-WOPI-ItemVersion", info.get("Version").toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(info);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(401).body("Token đã hết hạn");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token không hợp lệ");
        }
    }

    // 2. Get file
    @GetMapping("/{filename}/contents")
    public ResponseEntity<Resource> getFileContents(@PathVariable String filename) throws IOException {
        File file = new File(FILE_DIR, filename);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 3. PUT/POST contents – LƯU FILE
    @RequestMapping(
            value = "/{filename}/contents",
            method = {RequestMethod.POST, RequestMethod.PUT}
    )
    public ResponseEntity<?> saveFileContents(
            @PathVariable String filename,
            HttpServletRequest request) throws IOException {
        
        File tempFile = new File(FILE_DIR, filename);

        // 1. Đọc nội dung từ Collabora
        byte[] content = request.getInputStream().readAllBytes();

        // 2. Ghi vào file tạm trước
        Files.write(tempFile.toPath(), content);

        // 3. TÌM FILE GỐC (nếu là file tạm)
        DocumentFiles tempRecord = documentFileRepository.findByAttachName(filename);
        if (tempRecord != null && Boolean.TRUE.equals(tempRecord.getIsTemp()) && tempRecord.getOriginalFileId() != null) {
            UUID originalId = tempRecord.getOriginalFileId();
            DocumentFiles originalFile = documentFileRepository.findById(originalId)
                    .orElseThrow(() -> new RuntimeException("Original file not found"));

            // 4. GHI ĐÈ FILE GỐC
            Path originalPath = Paths.get(FILE_DIR, originalFile.getAttachName());
            Files.write(originalPath, content);
            System.out.println("GHI ĐÈ FILE GỐC: " + originalPath);

            // 5. XÓA FILE TẠM + BẢN GHI
            documentFileRepository.delete(tempRecord);
            Files.deleteIfExists(tempFile.toPath());
            System.out.println("ĐÃ XÓA FILE TẠM: " + tempFile.getPath());
        }

        return ResponseEntity.ok().build();
    }

    // 4. XỬ LÝ LOCK / UNLOCK – BẮT BUỘC KHI CHỈNH SỬA
    @PostMapping("/{filename}")
    public ResponseEntity<?> handlePost(
            @PathVariable String filename,
            @RequestHeader(value = "X-WOPI-Override", required = false) String override,
            @RequestHeader(value = "X-WOPI-Lock", required = false) String lock) {

        if ("LOCK".equalsIgnoreCase(override)) {
            return ResponseEntity.ok().build();
        }
        if ("UNLOCK".equalsIgnoreCase(override)) {
            return ResponseEntity.ok().build();
        }
        if ("GETLOCK".equalsIgnoreCase(override)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok().build();
    }
}
