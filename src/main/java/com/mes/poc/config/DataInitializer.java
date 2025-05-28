package com.mes.poc.config;

import com.mes.poc.model.Equipment;
import com.mes.poc.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EquipmentRepository equipmentRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeSampleData();
    }

    private void initializeSampleData() {
        if (equipmentRepository.count() == 0) {
            log.info("Initializing sample equipment data");

            Equipment equipment1 = Equipment.builder()
                    .equipmentCode("LINE_001")
                    .equipmentName("Production Line 1")
                    .location("Factory Floor A")
                    .status(Equipment.EquipmentStatus.RUNNING)
                    .build();

            Equipment equipment2 = Equipment.builder()
                    .equipmentCode("ROBOT_001")
                    .equipmentName("Assembly Robot 1")
                    .location("Factory Floor A")
                    .status(Equipment.EquipmentStatus.IDLE)
                    .build();

            Equipment equipment3 = Equipment.builder()
                    .equipmentCode("CONV_001")
                    .equipmentName("Conveyor Belt 1")
                    .location("Factory Floor B")
                    .status(Equipment.EquipmentStatus.RUNNING)
                    .build();

            equipmentRepository.save(equipment1);
            equipmentRepository.save(equipment2);
            equipmentRepository.save(equipment3);

            log.info("Sample equipment data initialized");
        }
    }
}
