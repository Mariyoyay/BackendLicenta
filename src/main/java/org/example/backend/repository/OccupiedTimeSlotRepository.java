package org.example.backend.repository;

import org.example.backend.model.OccupiedTimeSlot;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OccupiedTimeSlotRepository extends JpaRepository<OccupiedTimeSlot, Long> {
    List<OccupiedTimeSlot> findAllByDoctorAndStartTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);
}
