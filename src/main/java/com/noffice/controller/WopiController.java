package com.noffice.controller;

import com.noffice.ultils.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/wopi/files")
@CrossOrigin(
        origins = "${URL.COLLABORA.OFFICE}",
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class WopiController {

    private static final String FILE_DIR = AppConfig.get("save_path");

    @GetMapping("/{filename}")
    public ResponseEntity<?> checkFile(@PathVariable String filename) {
        File file = new File(FILE_DIR, filename);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("BaseFileName", filename);
        info.put("OwnerId", "admin");
        info.put("UserId", "admin");
        info.put("Size", file.length());
        info.put("Version", "1.0");
        info.put("SupportsLocks", true);
        info.put("SupportsUpdate", true);
        info.put("UserCanWrite", true);
        info.put("UserFriendlyName", "Administrator");
        info.put("PostMessageOrigin", AppConfig.get("URL.COLLABORA.OFFICE"));

        return ResponseEntity.ok()
                .header("X-WOPI-ItemVersion", "1.0")
                .contentType(MediaType.APPLICATION_JSON)
                .body(info);
    }

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

    @RequestMapping(value = "/{filename}/contents", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> saveFileContents(@PathVariable String filename, HttpServletRequest request) throws IOException {
        File file = new File(FILE_DIR, filename);
        try (InputStream in = request.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            in.transferTo(out);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{filename}")
    public ResponseEntity<?> handlePost(@PathVariable String filename,
                                        @RequestHeader(value = "X-WOPI-Override", required = false) String override) {
        if ("LOCK".equalsIgnoreCase(override) || "UNLOCK".equalsIgnoreCase(override)) {
            return ResponseEntity.ok().build();
        } else if ("PUT_RELATIVE".equalsIgnoreCase(override)) {
            Map<String, Object> res = new HashMap<>();
                res.put("Name", filename);
                res.put("Url", AppConfig.get("API.URL")+"/wopi/files/" + filename);
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.ok().build();
    }


}
