package org.example.backend.DTO;

import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.model.*;

import java.time.LocalDateTime;

import static org.example.backend.utils.TimeSlotTypeNames.APPOINTMENT;
import static org.example.backend.utils.TimeSlotTypeNames.OCCUPIED;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {

    private Long id;

    private String description;

    private LocalDateTime startTime;
    private Integer durationMinutes;

    private OfficeDTO office;

    private UserDTO doctor;

    private UserDTO patient;

    private UserDTO lastEditUser;
    private LocalDateTime lastEditTime;

    private Boolean isCanceled;

    private String type;

    // Constructor

    public TimeSlotDTO(TimeSlot timeSlot) {
        this.type = timeSlot.getType();
        switch (this.type) {
            case APPOINTMENT:{
                Appointment appointment = (Appointment) timeSlot;
                this.id = appointment.getId();
                this.description = appointment.getDescription();
                this.startTime = appointment.getStartTime();
                this.durationMinutes = appointment.getDurationMinutes();
                this.office = new OfficeDTO(appointment.getOffice());
                this.doctor = new UserDTO(appointment.getDoctor());
                this.patient = new UserDTO(appointment.getPatient());
                this.lastEditUser = new UserDTO(appointment.getLastEditUser());
                this.lastEditTime = appointment.getLastEditTime();
                this.isCanceled = appointment.getIsCanceled();
                break;
            }
            case OCCUPIED:{
                OccupiedTimeSlot occupiedTimeSlot = (OccupiedTimeSlot) timeSlot;
                this.id = occupiedTimeSlot.getId();
                this.description = occupiedTimeSlot.getDescription();
                this.startTime = occupiedTimeSlot.getStartTime();
                this.durationMinutes = occupiedTimeSlot.getDurationMinutes();
                this.doctor = new UserDTO(occupiedTimeSlot.getDoctor());
                break;
            }
            default:{
                break;
            }
        }
    }
}
