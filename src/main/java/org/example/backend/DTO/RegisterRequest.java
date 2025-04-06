package org.example.backend.DTO;

import lombok.Data;

import java.sql.Date;

@Data
public class RegisterRequest {
    private String email;
    private String password;

    private String firstName;
    private String lastName;

    private String phone;
    private Date dateOfBirth;
}
