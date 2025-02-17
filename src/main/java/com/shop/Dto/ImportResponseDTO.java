package com.shop.Dto;

import com.shop.Utils.BaseDTO;
import com.shop.Utils.PageDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ImportResponseDTO extends BaseDTO {
    private List<String> message;
    private PageDTO pageInfo;
}

