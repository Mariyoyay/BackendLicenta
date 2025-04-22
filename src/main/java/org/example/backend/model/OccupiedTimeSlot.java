package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "occupied_time_solts")
public class OccupiedTimeSlot implements TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description = "Occupied";

    private LocalDateTime startTime;
    private Integer durationMinutes;

    @ManyToOne
    private User doctor;

    @Override
    public User getDoctor() {
        return doctor;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public String getType() {
        return "OCCUPIED";
    }
}
