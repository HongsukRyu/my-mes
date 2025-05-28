package com.mes.poc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Equipment Entity
@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String equipmentCode;

    @Column(nullable = false)
    private String equipmentName;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum EquipmentStatus {
        RUNNING, IDLE, ALARM, MAINTENANCE, OFFLINE
    }
}