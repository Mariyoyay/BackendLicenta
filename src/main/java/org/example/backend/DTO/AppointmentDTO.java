package org.example.backend.DTO;

import lombok.Data;
import org.example.backend.model.User;

import java.util.Date;


@Data
public class AppointmentDTO {
    private Long id;
    private String description;
    private Date startTime;
    private Integer durationMinutes;
    private Long doctorID;
    private Long patientID;

//    private User lastEditUser;
//    private Date lastEditTime;
//    private Boolean isCanceled = false;
}
