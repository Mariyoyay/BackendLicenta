package org.example.backend.service;

import org.example.backend.DTO.TimeSlotDTO;
import org.example.backend.model.*;
import org.example.backend.repository.AppointmentRepository;
import org.example.backend.repository.OccupiedTimeSlotRepository;
import org.example.backend.repository.OfficeRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.example.backend.utils.RoleNames.*;

@Service
public class TimeSlotService {

    private final AppointmentRepository appointmentRepository;
    private final OccupiedTimeSlotRepository occupiedTimeSlotRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;

    public TimeSlotService(AppointmentRepository appointmentRepository, OccupiedTimeSlotRepository occ, UserRepository userRepository, OfficeRepository officeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.occupiedTimeSlotRepository = occ;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    public Appointment addAppointment(TimeSlotDTO appointmentDTO, String editorEmail) {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        Office office;
        if (officeRepository.existsById(appointmentDTO.getOfficeID())){
            office = officeRepository.findById(appointmentDTO.getOfficeID()).get();
        } else throw new RuntimeException("Invalid id. Office doesn't exist");
        newAppointment.setOffice(office);
//        System.out.println("-------------------------" + newAppointment.getOffice().getName() + " " + newAppointment.getOffice().getDoctors().stream().toList().get(0).getEmail());

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctorID())) {
            doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

//        System.out.println(doctor.getEmail() + doctor.getId());

        if(!doctor.getRolesAsString().contains(ROLE_DOCTOR))
            throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
        if(!newAppointment.getOffice().getDoctorIds().contains(doctor.getId()))
            throw new RuntimeException("Doctor doesn't work in this office");
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
    public Appointment updateAppointment(TimeSlotDTO appointmentDTO, String editorEmail) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentDTO.getId());
        if (appointmentOptional.isEmpty()) throw new RuntimeException("Invalid appointment id");
        Appointment appointment = appointmentOptional.get();

        if (appointmentDTO.getDescription() != null)
            appointment.setDescription(appointmentDTO.getDescription());
        if (appointmentDTO.getStartTime() != null)
            appointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        if (appointmentDTO.getDurationMinutes() != null)
            appointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        if (appointmentDTO.getIsCanceled() != null){
            appointment.setIsCanceled(appointmentDTO.getIsCanceled());
        }

        if (appointmentDTO.getOfficeID() != null) {
            Office office;
            if (officeRepository.existsById(appointmentDTO.getOfficeID())) {
                office = officeRepository.findById(appointmentDTO.getOfficeID()).get();
            } else throw new RuntimeException("Invalid id. Office doesn't exist");
            appointment.setOffice(office);
        }

        if (appointmentDTO.getDoctorID() != null) {
            User doctor;
            if (userRepository.existsById(appointmentDTO.getDoctorID())) {
                doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
            } else throw new RuntimeException("Invalid email. User doesn't exist");

            if (!doctor.getRolesAsString().contains(ROLE_DOCTOR))
                throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
            if (!appointment.getOffice().getDoctorIds().contains(doctor.getId()))
                throw new RuntimeException("Doctor doesn't work in this office");
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
    public Appointment deleteAppointment(Long appointmentID, String editorEmail) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentID);
        if (optionalAppointment.isEmpty()) throw new RuntimeException("Invalid occupied time slot id");
        Appointment appointment = optionalAppointment.get();

        if (!Objects.equals(appointment.getDoctor().getEmail(), editorEmail))
            throw new RuntimeException("Appointment you are trying to delete does not belong to this Doctor");

        appointmentRepository.delete(appointment);
        return appointment;
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
    public Appointment scheduleAppointment(TimeSlotDTO appointmentDTO, String patientEmail) {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        Office office;
        if (officeRepository.existsById(appointmentDTO.getOfficeID())){
            office = officeRepository.findById(appointmentDTO.getOfficeID()).get();
        } else throw new RuntimeException("Invalid id. Office doesn't exist");
        newAppointment.setOffice(office);

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctorID())) {
            doctor = userRepository.findById(appointmentDTO.getDoctorID()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if(!doctor.getRolesAsString().contains(ROLE_DOCTOR))
            throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
        if(!newAppointment.getOffice().getDoctors().contains(doctor))
            throw new RuntimeException("Doctor doesn't work in this office");
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

    @Transactional
    public OccupiedTimeSlot addOccupiedTimeSlot(TimeSlotDTO occupiedTimeSlotDTO, String doctorEmail) {
        OccupiedTimeSlot occupiedTimeSlot = new OccupiedTimeSlot();
        if (occupiedTimeSlotDTO.getDescription() != null)
            occupiedTimeSlot.setDescription(occupiedTimeSlotDTO.getDescription());
        occupiedTimeSlot.setStartTime(occupiedTimeSlotDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        occupiedTimeSlot.setDurationMinutes(occupiedTimeSlotDTO.getDurationMinutes());

        User doctor;
        if (userRepository.existsByEmail(doctorEmail)) {
            doctor = userRepository.findByEmail(doctorEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        occupiedTimeSlot.setDoctor(doctor);

        return occupiedTimeSlotRepository.save(occupiedTimeSlot);
    }

    @Transactional
    public OccupiedTimeSlot updateOccupiedTimeSlot(TimeSlotDTO occupiedTimeSlotDTO, String doctorEmail) {
        Optional<OccupiedTimeSlot> optionalOccupiedTimeSlot = occupiedTimeSlotRepository.findById(occupiedTimeSlotDTO.getId());
        if (optionalOccupiedTimeSlot.isEmpty()) throw new RuntimeException("Invalid occupied time slot id");
        OccupiedTimeSlot occupiedTimeSlot = optionalOccupiedTimeSlot.get();

        if (!Objects.equals(occupiedTimeSlot.getDoctor().getEmail(), doctorEmail))
            throw new RuntimeException("TimeSlot doesn't belong to this Doctor");

        if (occupiedTimeSlotDTO.getDescription() != null)
            occupiedTimeSlot.setDescription(occupiedTimeSlotDTO.getDescription());
        if (occupiedTimeSlotDTO.getStartTime() != null)
            occupiedTimeSlot.setStartTime(occupiedTimeSlotDTO.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        if (occupiedTimeSlotDTO.getDurationMinutes() != null)
            occupiedTimeSlot.setDurationMinutes(occupiedTimeSlotDTO.getDurationMinutes());

        return occupiedTimeSlotRepository.save(occupiedTimeSlot);
    }

    @Transactional
    public OccupiedTimeSlot deleteOccupiedTimeSlot(Long occupiedTimeSlotId, String doctorEmail) {
        Optional<OccupiedTimeSlot> optionalOccupiedTimeSlot = occupiedTimeSlotRepository.findById(occupiedTimeSlotId);
        if (optionalOccupiedTimeSlot.isEmpty()) throw new RuntimeException("Invalid occupied time slot id");
        OccupiedTimeSlot occupiedTimeSlot = optionalOccupiedTimeSlot.get();

        if (!Objects.equals(occupiedTimeSlot.getDoctor().getEmail(), doctorEmail))
            throw new RuntimeException("TimeSlot doesn't belong to this Doctor");

        occupiedTimeSlotRepository.delete(occupiedTimeSlot);
        return occupiedTimeSlot;
    }


    // READS

    @Transactional
    public List<TimeSlot> getDayActivitiesForOffice(Long officeId, Date date) {
        Office office;
        if (officeRepository.existsById(officeId)) {
            office = officeRepository.findById(officeId).get();
        } else throw new RuntimeException("Invalid email. Doctor doesn't exist");

        LocalDate dateAsLocalDate = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDateTime startTime = dateAsLocalDate.atStartOfDay();
        LocalDateTime endTime = dateAsLocalDate.atTime(23, 59, 59);

        List<TimeSlot> timeSlots = new ArrayList<>(appointmentRepository.findAllByOfficeAndStartTimeBetween(office, startTime, endTime));

        for (User doctor: office.getDoctors()) {
            timeSlots.addAll(occupiedTimeSlotRepository.findAllByDoctorAndStartTimeBetween(doctor, startTime,endTime));
        }

        return timeSlots;
    }

    @Transactional
    public List<TimeSlot> getDayActivitiesForDoctor(String doctorEmail, Date date) {
        User doctor;
        if (userRepository.existsByEmail(doctorEmail)) {
            doctor = userRepository.findByEmail(doctorEmail).get();
        } else throw new RuntimeException("Invalid email. Doctor doesn't exist");

        LocalDate dateAsLocalDate = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDateTime startTime = dateAsLocalDate.atStartOfDay();
        LocalDateTime endTime = dateAsLocalDate.atTime(23, 59, 59);

        List<TimeSlot> activities = new ArrayList<>();

        activities.addAll(appointmentRepository.findAllByDoctorAndStartTimeBetween(doctor, startTime,endTime));
        activities.addAll(occupiedTimeSlotRepository.findAllByDoctorAndStartTimeBetween(doctor, startTime,endTime));

        return activities;
    }
}
