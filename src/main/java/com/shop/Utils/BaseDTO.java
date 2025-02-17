package com.shop.Utils;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDTO {
    private Boolean success;
    private Integer statusCode;
    private String statusValue;
    private LocalDateTime executeDate;
}

