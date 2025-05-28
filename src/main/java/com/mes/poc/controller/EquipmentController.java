package com.mes.poc.controller;

import com.mes.poc.model.*;
import com.mes.poc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Equipment Controller
@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final ProcessDataRepository processDataRepository;

    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        List<Equipment> equipment = equipmentRepository.findAll();
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipment(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentRepository.findById(id);
        return equipment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{equipmentCode}")
    public ResponseEntity<Equipment> getEquipmentByCode(@PathVariable String equipmentCode) {
        Optional<Equipment> equipment = equipmentRepository.findByEquipmentCode(equipmentCode);
        return equipment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Equipment>> getEquipmentByStatus(@PathVariable Equipment.EquipmentStatus status) {
        List<Equipment> equipment = equipmentRepository.findByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        if (!equipmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipment.setId(id);
        Equipment updated = equipmentRepository.save(equipment);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/process-data")
    public ResponseEntity<List<ProcessData>> getEquipmentProcessData(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentRepository.findById(id);
        if (equipment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24); // Last 24 hours

        List<ProcessData> processData = processDataRepository
                .findByEquipmentAndTimeRange(id, startTime, endTime);

        return ResponseEntity.ok(processData);
    }
}
