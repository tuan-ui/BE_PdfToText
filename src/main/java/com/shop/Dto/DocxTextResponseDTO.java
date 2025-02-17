package com.shop.Dto;

import com.shop.Utils.BaseDTO;
import com.shop.Utils.PageDTO;
import com.shop.Entity.docxText.DocxText;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocxTextResponseDTO extends BaseDTO {
    private List<DocxText> listDocxText;
    private PageDTO pageInfo;
}
