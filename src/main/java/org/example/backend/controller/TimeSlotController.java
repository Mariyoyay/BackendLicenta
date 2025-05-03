package org.example.backend.controller;

import org.example.backend.DTO.TimeSlotDTO;
import org.example.backend.model.Appointment;
import org.example.backend.model.OccupiedTimeSlot;
import org.example.backend.model.TimeSlot;
import org.example.backend.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/time_slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/appointment/schedule")
    public ResponseEntity<?> scheduleAppointment(@RequestBody TimeSlotDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String patientEmail = (String) authentication.getPrincipal();

            Appointment addedAppointment = timeSlotService.scheduleAppointment(appointmentDTO, patientEmail);

            return ResponseEntity.ok(addedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/appointment/cancel")
    public ResponseEntity<?> cancelAppointment(@RequestBody TimeSlotDTO appointmentDTO) {

        Long appointmentId = appointmentDTO.getId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String editorEmail = (String) authentication.getPrincipal();

            Appointment canceledAppointment = timeSlotService.cancelAppointment(appointmentId, editorEmail);

            return ResponseEntity.ok(canceledAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/appointment/manage/add")
    public ResponseEntity<?> addAppointment(@RequestBody TimeSlotDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String editorEmail = (String) authentication.getPrincipal();

            Appointment addedAppointment = timeSlotService.addAppointment(appointmentDTO, editorEmail);

            return ResponseEntity.ok(addedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/appointment/manage/update")
    public ResponseEntity<?> updateAppointment(@RequestBody TimeSlotDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String editorEmail = (String) authentication.getPrincipal();

            Appointment editedAppointment = timeSlotService.updateAppointment(appointmentDTO, editorEmail);

            return ResponseEntity.ok(editedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/occupied/add")
    public ResponseEntity<?> addOccupiedTimeSlot(@RequestBody TimeSlotDTO occupiedTimeSlotDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            OccupiedTimeSlot occupiedTimeSlot = timeSlotService.addOccupiedTimeSlot(occupiedTimeSlotDTO, doctorEmail);

            return ResponseEntity.ok(occupiedTimeSlot);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/occupied/update")
    public ResponseEntity<?> updateOccupiedTimeSlot(@RequestBody TimeSlotDTO occupiedTimeSlotDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            OccupiedTimeSlot occupiedTimeSlot = timeSlotService.updateOccupiedTimeSlot(occupiedTimeSlotDTO, doctorEmail);

            return ResponseEntity.ok(occupiedTimeSlot);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @DeleteMapping("/occupied/delete")
    public ResponseEntity<?> deleteOccupiedTimeSlot(@RequestBody TimeSlotDTO occupiedTimeSlotDTO) {
        Long occupiedTimeSlotId = occupiedTimeSlotDTO.getId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            OccupiedTimeSlot occupiedTimeSlot = timeSlotService.deleteOccupiedTimeSlot(occupiedTimeSlotId, doctorEmail);

            return ResponseEntity.ok(occupiedTimeSlot);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @GetMapping("/day_schedule/{doctor_email}")
    public ResponseEntity<?> daySchedule(@PathVariable("doctor_email") String doctorEmail,
                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {

        List<TimeSlot> dayActivitiesList = timeSlotService.getDayActivities(doctorEmail, date);

        return ResponseEntity.ok(dayActivitiesList);
    }
}

