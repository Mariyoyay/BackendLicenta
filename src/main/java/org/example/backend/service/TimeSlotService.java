package org.example.backend.service;

import org.example.backend.DTO.TimeSlotDTO;
import org.example.backend.exceptions.OverlappingTimeSlotException;
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
    public Appointment addAppointment(TimeSlotDTO appointmentDTO, String editorEmail) throws OverlappingTimeSlotException {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        Office office;
        if (officeRepository.existsById(appointmentDTO.getOffice().getId())){
            office = officeRepository.findById(appointmentDTO.getOffice().getId()).get();
        } else throw new RuntimeException("Invalid id. Office doesn't exist");
        newAppointment.setOffice(office);

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctor().getId())) {
            doctor = userRepository.findById(appointmentDTO.getDoctor().getId()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if(!doctor.getRolesAsString().contains(ROLE_DOCTOR))
            throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
        if(!newAppointment.getOffice().getDoctorIds().contains(doctor.getId()))
            throw new RuntimeException("Doctor doesn't work in this office");
        newAppointment.setDoctor(doctor);

        User patient;
        if (userRepository.existsById(appointmentDTO.getPatient().getId())) {
            patient = userRepository.findById(appointmentDTO.getPatient().getId()).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        if (!patient.getRolesAsString().contains(ROLE_PATIENT))
            throw new RuntimeException("Patient doesn't have ROLE_PATIENT");
        newAppointment.setPatient(patient);

        if (this.isTimeSlotOverlapping(newAppointment)) throw new OverlappingTimeSlotException("The Office/Doctor/Patient is occupied at that time");

        User editor;
        if (userRepository.existsByEmail(editorEmail)) {
            editor = userRepository.findByEmail(editorEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        newAppointment.setLastEditUser(editor);
        newAppointment.setLastEditTime(LocalDateTime.now());

        return appointmentRepository.save(newAppointment);
    }

    @Transactional
    public Appointment updateAppointment(TimeSlotDTO appointmentDTO, String editorEmail) throws OverlappingTimeSlotException {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentDTO.getId());
        if (appointmentOptional.isEmpty()) throw new RuntimeException("Invalid appointment id");
        Appointment appointment = appointmentOptional.get();

        if (this.isTimeSlotOverlapping(appointment, appointmentDTO))
            throw new OverlappingTimeSlotException("The Office/Doctor/Patient is occupied at that time");


        if (appointmentDTO.getDescription() != null)
            appointment.setDescription(appointmentDTO.getDescription());
        if (appointmentDTO.getStartTime() != null)
            appointment.setStartTime(appointmentDTO.getStartTime());
        if (appointmentDTO.getDurationMinutes() != null)
            appointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        if (appointmentDTO.getIsCanceled() != null)
            appointment.setIsCanceled(appointmentDTO.getIsCanceled());


        if (appointmentDTO.getOffice().getId() != null) {
            Office office;
            if (officeRepository.existsById(appointmentDTO.getOffice().getId())) {
                office = officeRepository.findById(appointmentDTO.getOffice().getId()).get();
            } else throw new RuntimeException("Invalid id. Office doesn't exist");
            appointment.setOffice(office);
        }

        if (appointmentDTO.getDoctor().getId() != null) {
            User doctor;
            if (userRepository.existsById(appointmentDTO.getDoctor().getId())) {
                doctor = userRepository.findById(appointmentDTO.getDoctor().getId()).get();
            } else throw new RuntimeException("Invalid email. User doesn't exist");

            if (!doctor.getRolesAsString().contains(ROLE_DOCTOR))
                throw new RuntimeException("Doctor doesn't have ROLE_DOCTOR");
            if (!appointment.getOffice().getDoctorIds().contains(doctor.getId()))
                throw new RuntimeException("Doctor doesn't work in this office");
            appointment.setDoctor(doctor);
        }

        if (appointmentDTO.getPatient().getId() != null) {
            User patient;
            if (userRepository.existsById(appointmentDTO.getPatient().getId())) {
                patient = userRepository.findById(appointmentDTO.getPatient().getId()).get();
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

        return appointment;
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

        return appointment;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void deletePassedCanceledAppointmentsAndPassedOccupiedTimeSlots(){
        appointmentRepository.deleteCanceledAppointments(LocalDateTime.now());
        occupiedTimeSlotRepository.deletePassedOccupiedTimeSlots(LocalDateTime.now());
    }

    @Transactional
    public Appointment scheduleAppointment(TimeSlotDTO appointmentDTO, String patientEmail) {
        Appointment newAppointment = new Appointment();
        newAppointment.setDescription(appointmentDTO.getDescription());
        newAppointment.setStartTime(appointmentDTO.getStartTime());
        newAppointment.setDurationMinutes(appointmentDTO.getDurationMinutes());

        Office office;
        if (officeRepository.existsById(appointmentDTO.getOffice().getId())){
            office = officeRepository.findById(appointmentDTO.getOffice().getId()).get();
        } else throw new RuntimeException("Invalid id. Office doesn't exist");
        newAppointment.setOffice(office);

        User doctor;
        if (userRepository.existsById(appointmentDTO.getDoctor().getId())) {
            doctor = userRepository.findById(appointmentDTO.getDoctor().getId()).get();
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
    public OccupiedTimeSlot addOccupiedTimeSlot(TimeSlotDTO occupiedOldTimeSlotDTO, String doctorEmail) throws OverlappingTimeSlotException {
        OccupiedTimeSlot occupiedTimeSlot = new OccupiedTimeSlot();
        if (occupiedOldTimeSlotDTO.getDescription() != null)
            occupiedTimeSlot.setDescription(occupiedOldTimeSlotDTO.getDescription());
        occupiedTimeSlot.setStartTime(occupiedOldTimeSlotDTO.getStartTime());
        occupiedTimeSlot.setDurationMinutes(occupiedOldTimeSlotDTO.getDurationMinutes());

        User doctor;
        if (userRepository.existsByEmail(doctorEmail)) {
            doctor = userRepository.findByEmail(doctorEmail).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        occupiedTimeSlot.setDoctor(doctor);

        if (this.isTimeSlotOverlapping(occupiedTimeSlot)) throw new OverlappingTimeSlotException("OccupiedTimeSlot is overlapping with another existing appointment");

        return occupiedTimeSlotRepository.save(occupiedTimeSlot);
    }

    @Transactional
    public OccupiedTimeSlot updateOccupiedTimeSlot(TimeSlotDTO occupiedOldTimeSlotDTO, String doctorEmail) throws OverlappingTimeSlotException {
        Optional<OccupiedTimeSlot> optionalOccupiedTimeSlot = occupiedTimeSlotRepository.findById(occupiedOldTimeSlotDTO.getId());
        if (optionalOccupiedTimeSlot.isEmpty()) throw new RuntimeException("Invalid occupied time slot id");
        OccupiedTimeSlot occupiedTimeSlot = optionalOccupiedTimeSlot.get();

        if (this.isTimeSlotOverlapping(occupiedTimeSlot, occupiedOldTimeSlotDTO)) throw new OverlappingTimeSlotException("OccupiedTimeSlot is overlapping with another existing appointment");


        if (!Objects.equals(occupiedTimeSlot.getDoctor().getEmail(), doctorEmail))
            throw new RuntimeException("TimeSlot doesn't belong to this Doctor");

        if (occupiedOldTimeSlotDTO.getDescription() != null)
            occupiedTimeSlot.setDescription(occupiedOldTimeSlotDTO.getDescription());
        if (occupiedOldTimeSlotDTO.getStartTime() != null)
            occupiedTimeSlot.setStartTime(occupiedOldTimeSlotDTO.getStartTime());
        if (occupiedOldTimeSlotDTO.getDurationMinutes() != null)
            occupiedTimeSlot.setDurationMinutes(occupiedOldTimeSlotDTO.getDurationMinutes());


        return occupiedTimeSlot;
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

    // VALIDATIONS

    private Boolean isTimeSlotOverlapping(Appointment appointment) {
        LocalDateTime startTime = appointment.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(appointment.getDurationMinutes());

        Integer numberOverlappingAppointments = appointmentRepository.countOverlappingAppointmentsByOfficeAndByDoctorAndByPatient(startTime, endTime, appointment.getOffice().getId(), appointment.getDoctor().getId(), appointment.getPatient().getId());
        if (numberOverlappingAppointments > 0) return true;

        numberOverlappingAppointments = occupiedTimeSlotRepository.countOverlappingOccupiedTimeSlotsByDoctor(startTime, endTime, appointment.getDoctor().getId());
        return numberOverlappingAppointments > 0;
    }

    private Boolean isTimeSlotOverlapping(Appointment appointment, TimeSlotDTO updatedAppointmentDTO) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        Long appointmentID = appointment.getId();
        Long officeId;
        Long doctorId;
        Long patientId;

        if (updatedAppointmentDTO.getStartTime() == null) {
            startTime = appointment.getStartTime();
        } else {
            startTime = updatedAppointmentDTO.getStartTime();
        }

        if (updatedAppointmentDTO.getDurationMinutes() == null) {
            endTime = startTime.plusMinutes(appointment.getDurationMinutes());
        } else {
            endTime = startTime.plusMinutes(updatedAppointmentDTO.getDurationMinutes());
        }

        if (updatedAppointmentDTO.getOffice().getId() == null) {
            officeId = appointment.getOffice().getId();
        } else {
            officeId = updatedAppointmentDTO.getOffice().getId();
        }

        if (updatedAppointmentDTO.getDoctor().getId() == null) {
            doctorId = appointment.getDoctor().getId();
        } else {
            doctorId = updatedAppointmentDTO.getDoctor().getId();
        }

        if (updatedAppointmentDTO.getPatient().getId() == null) {
            patientId = appointment.getPatient().getId();
        } else {
            patientId = updatedAppointmentDTO.getPatient().getId();
        }


        Integer numberOverlappingAppointments =
                appointmentRepository
                        .countOverlappingAppointmentsByOfficeAndByDoctorAndByPatientExcludingCurrentAppointment(
                                startTime, endTime, appointmentID, officeId, doctorId, patientId);
        if (numberOverlappingAppointments > 0) return true;

        numberOverlappingAppointments = occupiedTimeSlotRepository.countOverlappingOccupiedTimeSlotsByDoctor(startTime, endTime, appointment.getDoctor().getId());
        return numberOverlappingAppointments > 0;
    }

    private Boolean isTimeSlotOverlapping(OccupiedTimeSlot occupiedTimeSlot) {

        LocalDateTime startTime = occupiedTimeSlot.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(occupiedTimeSlot.getDurationMinutes());

        Integer numberOverlappingAppointments = appointmentRepository.countOverlappingAppointmentsByDoctor(startTime, endTime, occupiedTimeSlot.getDoctor().getId());

        return numberOverlappingAppointments != 0;
    }

    private Boolean isTimeSlotOverlapping(OccupiedTimeSlot occupiedTimeSlot, TimeSlotDTO updatedOccupiedOldTimeSlotDTO) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        Long doctorId;

        if (updatedOccupiedOldTimeSlotDTO.getStartTime() == null) {
            startTime = occupiedTimeSlot.getStartTime();
        } else {
            startTime = updatedOccupiedOldTimeSlotDTO.getStartTime();
        }

        if (updatedOccupiedOldTimeSlotDTO.getDurationMinutes() == null) {
            endTime = startTime.plusMinutes(occupiedTimeSlot.getDurationMinutes());
        } else {
            endTime = startTime.plusMinutes(updatedOccupiedOldTimeSlotDTO.getDurationMinutes());
        }

        if (updatedOccupiedOldTimeSlotDTO.getDoctor().getId() == null) {
            doctorId = occupiedTimeSlot.getDoctor().getId();
        } else {
            doctorId = updatedOccupiedOldTimeSlotDTO.getDoctor().getId();
        }


        Integer numberOverlappingAppointments = appointmentRepository.countOverlappingAppointmentsByDoctor(startTime, endTime, doctorId);

        return numberOverlappingAppointments != 0;
    }
}
