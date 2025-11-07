package com.noffice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormSchemaSearchDTO {
	private String formName;
	private String formCode;
	private Boolean status;
	private Integer page = 0;
	private Integer size = 10;

}
