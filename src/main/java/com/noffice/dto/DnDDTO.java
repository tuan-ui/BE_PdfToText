package com.noffice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnDDTO {
    String id;
    JsonNode content;
}
