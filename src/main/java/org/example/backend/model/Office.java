package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "offices")
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ManyToMany(targetEntity = User.class, fetch = FetchType.EAGER)
    private Set<User> doctors;

    public Set<Long> getDoctorIds() {
        return doctors.stream().map(User::getId).collect(Collectors.toSet());
    }

}
