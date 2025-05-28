package com.mes.poc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Alarm Entity
@Entity
@Table(name = "alarms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(nullable = false)
    private String alarmCode;

    @Column(nullable = false)
    private String alarmMessage;

    @Enumerated(EnumType.STRING)
    private AlarmSeverity severity;

    @Enumerated(EnumType.STRING)
    private AlarmStatus status;

    @CreatedDate
    private LocalDateTime occurredAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime clearedAt;

    public enum AlarmSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlarmStatus {
        ACTIVE, ACKNOWLEDGED, CLEARED
    }
}