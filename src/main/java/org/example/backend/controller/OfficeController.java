package org.example.backend.controller;

import org.example.backend.DTO.OfficeDTO;
import org.example.backend.service.OfficeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offices")
public class OfficeController {
    private final OfficeService officeService;


    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getOffices() {
        return ResponseEntity.ok(officeService.findAll().stream().map(OfficeDTO::new).toList());
    }

    @GetMapping("/get/by_id/{office_id}")
    public ResponseEntity<?> getOfficeById(@PathVariable("office_id") Long officeId) {
        return ResponseEntity.ok(new OfficeDTO(officeService.findOfficeById(officeId)));
    }
}
