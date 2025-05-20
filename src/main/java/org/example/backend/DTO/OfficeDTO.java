package org.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.model.Office;
import org.example.backend.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeDTO {
    private Long id;

    private String name;

    private String description;

    private Set<UserDTO> doctors;

    // Constructors

    public OfficeDTO(Office office) {
        this.id = office.getId();
        this.name = office.getName();
        this.description = office.getDescription();
        this.doctors = office.getDoctors().stream().map(UserDTO::new).collect(Collectors.toSet());
    }
}
