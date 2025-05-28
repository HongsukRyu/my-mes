package com.mes.poc.controller;

import com.mes.poc.model.*;
import com.mes.poc.service.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Production Controller
@Slf4j
@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;
    private final ProductionDataRepository productionRepository;

    @PostMapping("/record")
    public ResponseEntity<ProductionData> recordProduction(@RequestBody ProductionRequest request) {
        ProductionData production = productionService.recordProduction(
                request.getEquipmentCode(),
                request.getLotNumber(),
                request.getProductCode(),
                request.getQuantity()
        );
        return ResponseEntity.ok(production);
    }

    @GetMapping("/equipment/{equipmentCode}/total")
    public ResponseEntity<Map<String, Object>> getTotalProduction(
            @PathVariable String equipmentCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Integer total = productionService.getTotalProduction(equipmentCode, startTime, endTime);

        Map<String, Object> response = new HashMap<>();
        response.put("equipmentCode", equipmentCode);
        response.put("startTime", startTime);
        response.put("endTime", endTime);
        response.put("totalProduction", total);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/{equipmentCode}/history")
    public ResponseEntity<List<ProductionData>> getProductionHistory(@PathVariable String equipmentCode) {
        List<ProductionData> history = productionService.getProductionHistory(equipmentCode);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/lot/{lotNumber}")
    public ResponseEntity<Page<ProductionData>> getProductionByLot(
            @PathVariable String lotNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ProductionData> production = productionRepository.findByLotNumber(lotNumber, pageable);

        return ResponseEntity.ok(production);
    }

    // Inner class for request body
    public static class ProductionRequest {
        private String equipmentCode;
        private String lotNumber;
        private String productCode;
        private Integer quantity;

        // Getters and setters
        public String getEquipmentCode() { return equipmentCode; }
        public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
