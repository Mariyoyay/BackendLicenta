package org.example.backend.DTO;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private Long expiresIn;
}
