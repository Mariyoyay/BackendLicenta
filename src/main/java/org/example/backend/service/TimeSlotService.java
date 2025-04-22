package org.example.backend.service;

import org.example.backend.DTO.AppointmentDTO;
import org.example.backend.model.Appointment;
import org.example.backend.model.User;
import org.example.backend.repository.AppointmentRepository;
import org.example.backend.repository.OccupiedTimeSlotRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.example.backend.utils.RoleNames.*;

@Service
public class TimeSlotService {

    private final AppointmentRepository appointmentRepository;
    private final OccupiedTimeSlotRepository occupiedTimeSlotRepository;
    private final UserRepository userRepository;

    public TimeSlotService(AppointmentRepository appointmentRepository, OccupiedTimeSlotRepository occ, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.occupiedTimeSlotRepository = occ;
        this.userRepository = userRepository;
    }

    @Transactional
    public Appointment addAppointment(AppointmentDTO appointmentDTO, String editorEmail) {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctorID())) {
            doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if(!doctor.getRolesAsString().contains(ROLE_DOCTOR))
            throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
        newAppointment.setDoctor(doctor);

        User patient;
        if (userRepository.existsById(appointmentDTO.getPatientID())) {
            patient = userRepository.findById(appointmentDTO.getPatientID()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if (!patient.getRolesAsString().contains(ROLE_PATIENT))
            throw new RuntimeException("Patient doesn't have ROLE_PATIENT");
        newAppointment.setPatient(patient);

        User editor;
        if (userRepository.existsByEmail(editorEmail)) {
            editor = userRepository.findByEmail(editorEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        newAppointment.setLastEditUser(editor);
        newAppointment.setLastEditTime(LocalDateTime.now());

        return appointmentRepository.save(newAppointment);
    }

    @Transactional
    public Appointment updateAppointment(AppointmentDTO appointmentDTO, String editorEmail) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentDTO.getId());
        if (appointmentOptional.isEmpty()) throw new RuntimeException("Invalid appointment id");
        Appointment appointment = appointmentOptional.get();

        if (appointmentDTO.getDescription() != null)
            appointment.setDescription(appointmentDTO.getDescription());
        if (appointmentDTO.getStartTime() != null)
            appointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        if (appointmentDTO.getDurationMinutes() != null)
            appointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        if (appointmentDTO.getDoctorID() != null) {
            User doctor;
            if (userRepository.existsById(appointmentDTO.getDoctorID())) {
                doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
            } else throw new RuntimeException("Invalid email. User doesn't exist");

            if (!doctor.getRolesAsString().contains(ROLE_DOCTOR))
                throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
            appointment.setDoctor(doctor);
        }

        if (appointmentDTO.getPatientID() != null) {
            User patient;
            if (userRepository.existsById(appointmentDTO.getPatientID())) {
                patient = userRepository.findById(appointmentDTO.getPatientID()).get();
            } else throw new RuntimeException("Invalid email. User doesn't exist");

            if (!patient.getRolesAsString().contains(ROLE_PATIENT))
                throw new RuntimeException("Patient doesn't have ROLE_PATIENT");
            appointment.setPatient(patient);
        }
        User editor;
        if (userRepository.existsByEmail(editorEmail)) {
            editor = userRepository.findByEmail(editorEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        appointment.setLastEditUser(editor);
        appointment.setLastEditTime(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String editorEmail) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);
        if (appointmentOptional.isEmpty()) throw new RuntimeException("Invalid appointment id");
        Appointment appointment = appointmentOptional.get();

        if(!Objects.equals(appointment.getPatient().getEmail(), editorEmail)){
            User editor;
            if (userRepository.existsByEmail(editorEmail)) {
                editor = userRepository.findByEmail(editorEmail).get();
            } else throw new RuntimeException("Invalid email. User doesn't exist");

            if (!editor.getRolesAsString().contains(ROLE_DOCTOR) && !editor.getRolesAsString().contains(ROLE_EMPLOYEE))
                throw new RuntimeException("Appointment doesn't belong to current user");

            appointment.setLastEditUser(editor);
        } else {
            appointment.setLastEditUser(appointment.getPatient());
        }

        appointment.setIsCanceled(true);
        appointment.setLastEditTime(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deletePassedCanceledAppointments(){
        appointmentRepository.deleteCanceledAppointments(LocalDateTime.now());
    }

    @Transactional
    public Appointment makeAppointment(AppointmentDTO appointmentDTO, String patientEmail) {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctorID())) {
            doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if(!doctor.getRolesAsString().contains(ROLE_DOCTOR))
            throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
        newAppointment.setDoctor(doctor);

        User patient;
        if (userRepository.existsByEmail(patientEmail)) {
            patient = userRepository.findByEmail(patientEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        newAppointment.setPatient(patient);
        newAppointment.setLastEditUser(patient);

        newAppointment.setLastEditTime(LocalDateTime.now());

        return appointmentRepository.save(newAppointment);
    }
}
