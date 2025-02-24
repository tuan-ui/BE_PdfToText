package com.shop.Controller.ImageToText;

import com.shop.Utils.PageableEnum;
import com.shop.Dto.DocxTextResponseDTO;
import com.shop.Dto.ImportResponseDTO;
import com.shop.Service.imageToText.ImageToTextService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:5173")
public class ImageToTextController {

    @Autowired
    private ImageToTextService imageToTextService;

    @PostMapping("ImportFile")
    public ResponseEntity<ImportResponseDTO> importFile(@RequestParam("files") List<MultipartFile> files){
        new ImportResponseDTO();
        ImportResponseDTO response = imageToTextService.pdfToText(files);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("Search")
    public ResponseEntity<DocxTextResponseDTO> searchFile(@RequestParam("search") String search,
                                                          @RequestParam(value = "size", required = false) Integer pageSize,
                                                          @RequestParam(value = "page", required = false) Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber == null ? PageableEnum.ZERO.getValue() : pageNumber,
                ( pageSize == null ? PageableEnum.DEFAULT.getValue() : pageSize) == PageableEnum.ALL.getValue() ? Integer.MAX_VALUE : ( pageSize == null ? PageableEnum.DEFAULT.getValue() : pageSize));
        DocxTextResponseDTO response = imageToTextService.searchText(search,pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("GetAllFiles")
    public ResponseEntity<DocxTextResponseDTO> searchFile(@RequestParam(value = "size", required = false) Integer pageSize,
                                                          @RequestParam(value = "page", required = false) Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber == null ? PageableEnum.ZERO.getValue() : pageNumber,
                ( pageSize == null ? PageableEnum.DEFAULT.getValue() : pageSize) == PageableEnum.ALL.getValue() ? Integer.MAX_VALUE : ( pageSize == null ? PageableEnum.DEFAULT.getValue() : pageSize));
        DocxTextResponseDTO response = imageToTextService.getAllDocxText(pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("ExtractIdCardInfo")
    public ResponseEntity<List<String>> extractIdCardInfo(@RequestParam("files") MultipartFile files) throws TesseractException, IOException {
        new ImportResponseDTO();
        List<String> response = imageToTextService.extractIdCardInfo(files);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

