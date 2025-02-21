package com.shop.Controller.updateDoc;

import com.shop.Service.updateDoc.UpdateDocService;
import com.shop.Dto.ImportResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/doc")
@CrossOrigin(origins = "http://localhost:5173")
public class UpdateDocController {

    @Autowired
    private UpdateDocService updateDocService;

    @PostMapping("update")
    public ResponseEntity<String> importFile(){
        new ImportResponseDTO();
        String response = updateDocService.updateDoc();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extractText(@RequestParam("files") MultipartFile file) {
        try {
            Map<String, String> extractedInfo = updateDocService.extractIDCardInfo(file);
            return ResponseEntity.ok(extractedInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("addColumn")
    public ResponseEntity<String> addColumn(){
        new ImportResponseDTO();
        String response = updateDocService.addColumnToTable();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}


