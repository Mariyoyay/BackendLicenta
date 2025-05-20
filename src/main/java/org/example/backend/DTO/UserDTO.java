package org.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.model.User;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;

    private String email;

    private String firstName;
    private String lastName;

    private Date dateOfBirth;
    private String phone;

    private String color;

    // String of the Role Names e.g. "ROLE_ADMIN"
    private Set<String> roles;

    // Constructors

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.dateOfBirth = user.getDateOfBirth();
        this.phone = user.getPhone();
        this.color = user.getColor();
        this.roles = user.getRolesAsString();
    }
}
