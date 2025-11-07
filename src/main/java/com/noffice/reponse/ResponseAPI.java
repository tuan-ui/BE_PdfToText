package com.noffice.reponse;

import lombok.*;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class ResponseAPI {
    private Object object;
    private String message;
    private int status;
}
