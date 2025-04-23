package org.example.backend.repository;

import org.example.backend.model.Appointment;
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
}
