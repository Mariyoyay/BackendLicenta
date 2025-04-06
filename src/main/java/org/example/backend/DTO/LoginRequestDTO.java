package org.example.backend.DTO;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}
