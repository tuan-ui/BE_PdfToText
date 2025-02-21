package com.shop.Controller.updateDoc;

import com.shop.Service.updateDoc.UpdateDocService;
import com.shop.Dto.ImportResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}


