package org.example.backend.controller;

import org.example.backend.DTO.TimeSlotDTO;
import org.example.backend.exceptions.OverlappingTimeSlotException;
import org.example.backend.model.Appointment;
import org.example.backend.model.OccupiedTimeSlot;
import org.example.backend.model.TimeSlot;
import org.example.backend.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

            Appointment addedAppointment = null;
            try {
                addedAppointment = timeSlotService.addAppointment(appointmentDTO, editorEmail);
            } catch (OverlappingTimeSlotException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.ok(addedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/appointment/manage/update")
    public ResponseEntity<?> updateAppointment(@RequestBody TimeSlotDTO appointmentDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            String editorEmail = (String) authentication.getPrincipal();

            Appointment editedAppointment = null;
            try {
                editedAppointment = timeSlotService.updateAppointment(appointmentDTO, editorEmail);
            } catch (OverlappingTimeSlotException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.ok(editedAppointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @DeleteMapping("/appointment/manage/delete")
    public ResponseEntity<?> deleteAppointment(@RequestBody TimeSlotDTO appointmentDTO) {
        Long appointmentID = appointmentDTO.getId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String editorEmail = (String) authentication.getPrincipal();

            Appointment appointment = timeSlotService.deleteAppointment(appointmentID, editorEmail);

            return ResponseEntity.ok(appointment);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/occupied/add")
    public ResponseEntity<?> addOccupiedTimeSlot(@RequestBody TimeSlotDTO occupiedTimeSlotDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            OccupiedTimeSlot occupiedTimeSlot;

            try {
                occupiedTimeSlot = timeSlotService.addOccupiedTimeSlot(occupiedTimeSlotDTO, doctorEmail);
            } catch (OverlappingTimeSlotException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.ok(occupiedTimeSlot);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/occupied/update")
    public ResponseEntity<?> updateOccupiedTimeSlot(@RequestBody TimeSlotDTO occupiedTimeSlotDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            OccupiedTimeSlot occupiedTimeSlot = null;
            try {
                occupiedTimeSlot = timeSlotService.updateOccupiedTimeSlot(occupiedTimeSlotDTO, doctorEmail);
            } catch (OverlappingTimeSlotException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

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

    @GetMapping("/day_schedule/doctor/{doctor_email}")
    public ResponseEntity<?> daySchedule(@PathVariable("doctor_email") String doctorEmail,
                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {

        List<TimeSlot> dayActivitiesList = timeSlotService.getDayActivitiesForDoctor(doctorEmail, date);

        return ResponseEntity.ok(dayActivitiesList);
    }

    @GetMapping ("/day_schedule/office/{office_id}")
    public ResponseEntity<?> daySchedule(@PathVariable("office_id") Long officeId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {

        List<TimeSlot> dayActivitiesList = timeSlotService.getDayActivitiesForOffice(officeId, date);

        return ResponseEntity.ok(dayActivitiesList);
    }
}

