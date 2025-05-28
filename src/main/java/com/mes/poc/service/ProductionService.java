package com.mes.poc.service;

import com.mes.poc.model.*;
import com.mes.poc.repository.EquipmentRepository;
import com.mes.poc.repository.ProductionDataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Production Service
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionDataRepository productionRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public ProductionData recordProduction(String equipmentCode, String lotNumber,
                                           String productCode, Integer quantity) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByEquipmentCode(equipmentCode);
        if (equipmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Equipment not found: " + equipmentCode);
        }

        ProductionData production = ProductionData.builder()
                .equipment(equipmentOpt.get())
                .lotNumber(lotNumber)
                .productCode(productCode)
                .actualQuantity(quantity)
                .timestamp(LocalDateTime.now())
                .build();

        production = productionRepository.save(production);
        log.info("Recorded production: {} units of {} for lot {}",
                quantity, productCode, lotNumber);

        return production;
    }

    public Integer getTotalProduction(String equipmentCode, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByEquipmentCode(equipmentCode);
        if (equipmentOpt.isEmpty()) {
            return 0;
        }

        Integer total = productionRepository.getTotalProduction(
                equipmentOpt.get().getId(), startTime, endTime);

        return total != null ? total : 0;
    }

    public List<ProductionData> getProductionHistory(String equipmentCode) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByEquipmentCode(equipmentCode);
        if (equipmentOpt.isEmpty()) {
            return List.of();
        }

        return productionRepository.findByEquipmentOrderByTimestampDesc(equipmentOpt.get());
    }
}
