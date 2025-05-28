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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Alarm Controller
@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final AlarmRepository alarmRepository;

    @GetMapping("/active")
    public ResponseEntity<List<Alarm>> getActiveAlarms() {
        List<Alarm> alarms = alarmService.getActiveAlarms();
        return ResponseEntity.ok(alarms);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Alarm>> getAlarmsBySeverity(@PathVariable Alarm.AlarmSeverity severity) {
        List<Alarm> alarms = alarmService.getAlarmsBySeverity(severity);
        return ResponseEntity.ok(alarms);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAlarmStatistics() {
        Map<String, Object> stats = new HashMap<>();

        for (Alarm.AlarmSeverity severity : Alarm.AlarmSeverity.values()) {
            long count = alarmRepository.countByStatusAndSeverity(Alarm.AlarmStatus.ACTIVE, severity);
            stats.put(severity.name().toLowerCase() + "Count", count);
        }

        long totalActive = alarmRepository.countByStatusAndSeverity(Alarm.AlarmStatus.ACTIVE, null);
        stats.put("totalActiveCount", totalActive);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{alarmId}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlarm(@PathVariable Long alarmId) {
        alarmService.acknowledgeAlarm(alarmId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{alarmId}/clear")
    public ResponseEntity<Void> clearAlarm(@PathVariable Long alarmId) {
        alarmService.clearAlarm(alarmId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<Alarm>> getAllAlarms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("occurredAt").descending());
        Page<Alarm> alarms = alarmRepository.findAll(pageable);

        return ResponseEntity.ok(alarms);
    }
}
