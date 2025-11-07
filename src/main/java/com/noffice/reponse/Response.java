package com.noffice.reponse;

import lombok.*;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Response {
    private Object data;
    private String message;
    private int status;
}
