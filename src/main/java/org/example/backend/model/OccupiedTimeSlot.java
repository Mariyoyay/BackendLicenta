package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "occupied_time_slots")
public class OccupiedTimeSlot implements TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
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
