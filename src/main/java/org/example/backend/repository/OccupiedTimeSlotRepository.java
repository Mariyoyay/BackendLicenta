package org.example.backend.repository;

import org.example.backend.model.OccupiedTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupiedTimeSlotRepository extends JpaRepository<OccupiedTimeSlot, Long> {

}
