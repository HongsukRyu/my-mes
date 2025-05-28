package com.mes.poc.controller;

import com.mes.poc.model.*;
import com.mes.poc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Process Data Controller
@Slf4j
@RestController
@RequestMapping("/api/process-data")
@RequiredArgsConstructor
public class ProcessDataController {

    private final ProcessDataRepository processDataRepository;
    private final EquipmentRepository equipmentRepository;

    @GetMapping
    public ResponseEntity<Page<ProcessData>> getProcessData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProcessData> processData = processDataRepository.findAll(pageable);

        return ResponseEntity.ok(processData);
    }

    @GetMapping("/equipment/{equipmentCode}/parameter/{parameterName}")
    public ResponseEntity<List<ProcessData>> getProcessDataByParameter(
            @PathVariable String equipmentCode,
            @PathVariable String parameterName) {

        Optional<Equipment> equipment = equipmentRepository.findByEquipmentCode(equipmentCode);
        if (equipment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProcessData> processData = processDataRepository
                .findByEquipmentAndParameterNameOrderByTimestampDesc(equipment.get(), parameterName);

        return ResponseEntity.ok(processData);
    }

    @GetMapping("/range")
    public ResponseEntity<List<ProcessData>> getProcessDataByTimeRange(
            @RequestParam Long equipmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<ProcessData> processData = processDataRepository
                .findByEquipmentAndTimeRange(equipmentId, startTime, endTime);

        return ResponseEntity.ok(processData);
    }
}
