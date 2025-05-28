package com.mes.poc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Production Data Entity
@Entity
@Table(name = "production_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column
    private String lotNumber;

    @Column
    private String productCode;

    @Column
    private Integer targetQuantity;

    @Column
    private Integer actualQuantity;

    @Column
    private Integer defectQuantity;

    @CreatedDate
    private LocalDateTime timestamp;
}