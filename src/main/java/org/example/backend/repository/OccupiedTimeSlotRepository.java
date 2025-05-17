package org.example.backend.repository;

import jakarta.transaction.Transactional;
import org.example.backend.model.OccupiedTimeSlot;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OccupiedTimeSlotRepository extends JpaRepository<OccupiedTimeSlot, Long> {
    List<OccupiedTimeSlot> findAllByDoctorAndStartTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    @Query("DELETE FROM OccupiedTimeSlot ots WHERE ots.startTime < :today")
    void deletePassedOccupiedTimeSlots(@Param("today") LocalDateTime today);

    @Transactional
    @Query(value = """
        SELECT COUNT(*) FROM occupied_time_slots ots WHERE ots.doctor_id = :doctor_id AND (
            (:start_time < ots.start_time AND ots.start_time < :end_time)
                OR
            (
                :start_time < (ots.start_time + (ots.duration_minutes || ' minutes')::interval)
                    AND
                (ots.start_time + (ots.duration_minutes || ' minutes')::interval) < :end_time
            )
                OR
            (ots.start_time <= :start_time AND :end_time <= ots.start_time + (ots.duration_minutes || ' minutes')::interval)
        )
    """, nativeQuery = true)
    Integer countOverlappingOccupiedTimeSlotsByDoctor(@Param("start_time") LocalDateTime startTime,
                                                      @Param("end_time") LocalDateTime endTime,
                                                      @Param("doctor_id") Long doctorId);

}
