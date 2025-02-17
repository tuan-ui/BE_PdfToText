package com.shop.Utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageDTO {
    private Integer totalPages;
    private Integer totalElements;
    private Integer number;
    private Integer size;
}
