package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import static org.example.backend.utils.TimeSlotTypeNames.APPOINTMENT;

@Data
@Entity
@Table(name = "appointments")
public class Appointment implements TimeSlot{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private LocalDateTime startTime;
    private Integer durationMinutes;

    @ManyToOne
    private Office office;

    @ManyToOne
    private User doctor;

    @ManyToOne
    private User patient;

    @ManyToOne
    private User lastEditUser;
    private LocalDateTime lastEditTime;

    private Boolean isCanceled = false;

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
        return APPOINTMENT;
    }
}
