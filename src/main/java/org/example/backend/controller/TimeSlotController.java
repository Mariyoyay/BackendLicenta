package org.example.backend.controller;

import org.example.backend.DTO.AppointmentDTO;
import org.example.backend.model.Appointment;
import org.example.backend.model.TimeSlot;
import org.example.backend.model.User;
import org.example.backend.service.TimeSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time_slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/addAppointment")
    public ResponseEntity<?> addAppointment(@RequestBody AppointmentDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String editorEmail = (String) authentication.getPrincipal();

            Appointment addedAppointment = timeSlotService.addAppointment(appointmentDTO, editorEmail);

            return ResponseEntity.ok(addedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/updateAppointment")
    public ResponseEntity<?> updateAppointment(@RequestBody AppointmentDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String editorEmail = (String) authentication.getPrincipal();

            Appointment editedAppointment = timeSlotService.updateAppointment(appointmentDTO, editorEmail);

            return ResponseEntity.ok(editedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }
}
