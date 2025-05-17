package org.example.backend.repository;

import org.example.backend.model.Appointment;
import org.example.backend.model.Office;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.isCanceled AND a.startTime < :today")
    void deleteCanceledAppointments(@Param("today") LocalDateTime today);

    List<Appointment> findAllByDoctorAndStartTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByOfficeAndStartTimeBetween(Office office, LocalDateTime start, LocalDateTime end);

    @Transactional
    @Query(value = """
        SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctor_id
                                              AND a.is_canceled = false AND (
            (:start_time < a.start_time AND a.start_time < :end_time)
                OR
            (
                :start_time < (a.start_time + (a.duration_minutes || ' minutes')::interval)
                    AND
                (a.start_time + (a.duration_minutes || ' minutes')::interval) < :end_time
            )
                OR
            (a.start_time <= :start_time AND :end_time <= a.start_time + (a.duration_minutes || ' minutes')::interval)
        )
    """, nativeQuery = true)
    Integer countOverlappingAppointmentsByDoctor(@Param("start_time") LocalDateTime startTime,
                                                 @Param("end_time") LocalDateTime endTime,
                                                 @Param("doctor_id") Long doctor_id);

    @Transactional
    @Query(value = """
        SELECT COUNT(*) FROM appointments a WHERE
                                              (a.office_id = :office_id OR a.doctor_id = :doctor_id OR a.patient_id = :patient_id OR a.doctor_id = :patient_id OR a.patient_id = :doctor_id) AND
                                              a.is_canceled = false AND (
              (:start_time < a.start_time AND a.start_time < :end_time)
                  OR
              (:start_time < (a.start_time + (a.duration_minutes || ' minutes')::interval) AND (a.start_time + (a.duration_minutes || ' minutes')::interval) < :end_time)
                  OR
              (a.start_time <= :start_time AND :end_time <= a.start_time + (a.duration_minutes || ' minutes')::interval)
                                                                            )
    """, nativeQuery = true)
    Integer countOverlappingAppointmentsByOfficeAndByDoctorAndByPatient(@Param("start_time") LocalDateTime startTime,
                                                                        @Param("end_time") LocalDateTime endTime,
                                                                        @Param("office_id") Long office_id,
                                                                        @Param("doctor_id") Long doctor_id,
                                                                        @Param("patient_id") Long patient_id);

    @Transactional
    @Query(value = """
        SELECT COUNT(*) FROM appointments a WHERE a.id <> :current_appointment_id AND
                                              (a.office_id = :office_id OR a.doctor_id = :doctor_id OR a.patient_id = :patient_id OR a.doctor_id = :patient_id OR a.patient_id = :doctor_id) AND
                                              a.is_canceled = false AND (
              (:start_time < a.start_time AND a.start_time < :end_time)
                  OR
              (:start_time < (a.start_time + (a.duration_minutes || ' minutes')::interval) AND (a.start_time + (a.duration_minutes || ' minutes')::interval) < :end_time)
                  OR
              (a.start_time <= :start_time AND :end_time <= a.start_time + (a.duration_minutes || ' minutes')::interval)
                                                                            )
    """, nativeQuery = true)
    Integer countOverlappingAppointmentsByOfficeAndByDoctorAndByPatientExcludingCurrentAppointment(@Param("start_time") LocalDateTime startTime,
                                                                                                   @Param("end_time") LocalDateTime endTime,
                                                                                                   @Param("current_appointment_id") Long current_appointment_id,
                                                                                                   @Param("office_id") Long office_id,
                                                                                                   @Param("doctor_id") Long doctor_id,
                                                                                                   @Param("patient_id") Long patient_id);

}

