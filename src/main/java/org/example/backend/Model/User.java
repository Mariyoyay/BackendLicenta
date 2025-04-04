package org.example.backend.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    public User() {}

    public User(String email) {
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}
