package org.example.backend.service;

import org.example.backend.model.Office;
import org.example.backend.repository.OfficeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficeService {
    private final OfficeRepository officeRepository;

    public OfficeService(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }


    public List<Office> findAll() {
        return officeRepository.findAll();
    }

    public Office findOfficeById(Long officeId) {
        Office office;
        if (officeRepository.existsById(officeId)) {
            office = officeRepository.findById(officeId).get();
        } else throw new RuntimeException("Office not found");
        return office;
    }
}
