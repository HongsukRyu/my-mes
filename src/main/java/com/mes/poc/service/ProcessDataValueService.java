package com.mes.poc.service;

import com.mes.poc.config.OpcUaConfigProperties;
import com.mes.poc.model.Alarm;
import com.mes.poc.model.Equipment;
import com.mes.poc.model.ProcessData;
import com.mes.poc.repository.EquipmentRepository;
import com.mes.poc.repository.ProcessDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDataValueService {

    private final ProcessDataRepository processDataRepository;
    private final EquipmentRepository equipmentRepository;

    private final AlarmService alarmService;

    @Transactional
    protected void processDataValue(OpcUaConfigProperties.NodeConfiguration nodeConfig, DataValue dataValue) {
        // Find or create equipment (assuming equipment code is derived from node name)
        String equipmentCode = extractEquipmentCode(nodeConfig.getName());
        Equipment equipment = equipmentRepository.findByEquipmentCode(equipmentCode)
                .orElseGet(() -> createDefaultEquipment(equipmentCode));

        // Save process data
        ProcessData processData = ProcessData.builder()
                .equipment(equipment)
                .parameterName(nodeConfig.getName())
                .parameterValue(dataValue.getValue().getValue().toString())
                .nodeId(nodeConfig.getNodeId())
                .timestamp(LocalDateTime.now())
                .build();

        processDataRepository.save(processData);

        // Check for alarms
        checkAlarmConditions(equipment, nodeConfig, dataValue);

        log.debug("Processed data for {}: {}", nodeConfig.getName(),
                dataValue.getValue().getValue());
    }

    private String extractEquipmentCode(String parameterName) {
        // Simple logic to extract equipment code from parameter name
        // In real implementation, this would be more sophisticated
        return "EQUIP_" + parameterName.hashCode() % 1000;
    }

    private Equipment createDefaultEquipment(String equipmentCode) {
        Equipment equipment = Equipment.builder()
                .equipmentCode(equipmentCode)
                .equipmentName("Auto-created Equipment")
                .location("Unknown")
                .status(Equipment.EquipmentStatus.IDLE)
                .build();

        return equipmentRepository.save(equipment);
    }

    private void checkAlarmConditions(Equipment equipment,
                                     OpcUaConfigProperties.NodeConfiguration nodeConfig,
                                     DataValue dataValue) {
        // Example alarm conditions
        if ("Temperature".equals(nodeConfig.getName())) {
            double temperature = Double.parseDouble(dataValue.getValue().getValue().toString());
            if (temperature > 80.0) {
                Alarm alarm = alarmService.createAlarm(equipment, "TEMP_HIGH",
                        "Temperature exceeded 80°C: " + temperature,
                        Alarm.AlarmSeverity.HIGH);
            }
        }

        if ("EquipmentStatus".equals(nodeConfig.getName())) {
            String status = dataValue.getValue().getValue().toString();
            if ("ALARM".equals(status)) {
                Alarm alarm = alarmService.createAlarm(equipment, "EQUIP_ALARM",
                        "Equipment reported alarm status",
                        Alarm.AlarmSeverity.CRITICAL);
            }
        }
    }
}
