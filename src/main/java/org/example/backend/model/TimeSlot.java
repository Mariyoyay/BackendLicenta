package org.example.backend.model;


import java.time.LocalDateTime;

public interface TimeSlot {
        User getDoctor();

        LocalDateTime getStartTime();

        Integer getDurationMinutes();

        String getType();
}
