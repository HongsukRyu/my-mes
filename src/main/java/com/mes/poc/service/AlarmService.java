package com.mes.poc.service;

import com.mes.poc.model.Alarm;
import com.mes.poc.model.Equipment;
import com.mes.poc.repository.AlarmRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Alarm Service
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    @Transactional
    public Alarm createAlarm(Equipment equipment, String alarmCode,
                             String message, Alarm.AlarmSeverity severity) {
        // Check if similar alarm already exists and is active
        List<Alarm> existingAlarms = alarmRepository
                .findByEquipmentAndStatusOrderByOccurredAtDesc(equipment, Alarm.AlarmStatus.ACTIVE);

        boolean alarmExists = existingAlarms.stream()
                .anyMatch(alarm -> alarm.getAlarmCode().equals(alarmCode));

        if (alarmExists) {
            log.debug("Alarm {} already exists for equipment {}", alarmCode, equipment.getEquipmentCode());
            return null;
        }

        Alarm alarm = Alarm.builder()
                .equipment(equipment)
                .alarmCode(alarmCode)
                .alarmMessage(message)
                .severity(severity)
                .status(Alarm.AlarmStatus.ACTIVE)
                .occurredAt(LocalDateTime.now())
                .build();

        alarm = alarmRepository.save(alarm);
        log.info("Created alarm: {} for equipment: {}", alarmCode, equipment.getEquipmentCode());

        return alarm;
    }

    @Transactional
    public void acknowledgeAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (alarmOpt.isPresent()) {
            Alarm alarm = alarmOpt.get();
            alarm.setStatus(Alarm.AlarmStatus.ACKNOWLEDGED);
            alarm.setAcknowledgedAt(LocalDateTime.now());
            alarmRepository.save(alarm);

            log.info("Acknowledged alarm: {}", alarm.getAlarmCode());
        }
    }

    @Transactional
    public void clearAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (alarmOpt.isPresent()) {
            Alarm alarm = alarmOpt.get();
            alarm.setStatus(Alarm.AlarmStatus.CLEARED);
            alarm.setClearedAt(LocalDateTime.now());
            alarmRepository.save(alarm);

            log.info("Cleared alarm: {}", alarm.getAlarmCode());
        }
    }

    public List<Alarm> getActiveAlarms() {
        return alarmRepository.findByStatusOrderByOccurredAtDesc(Alarm.AlarmStatus.ACTIVE);
    }

    public List<Alarm> getAlarmsBySeverity(Alarm.AlarmSeverity severity) {
        return alarmRepository.findActiveBySeverity(severity);
    }
}