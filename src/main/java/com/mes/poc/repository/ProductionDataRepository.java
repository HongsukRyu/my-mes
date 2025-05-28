package com.mes.poc.repository;

import com.mes.poc.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionDataRepository extends JpaRepository<ProductionData, Long> {

    List<ProductionData> findByEquipmentOrderByTimestampDesc(Equipment equipment);

    @Query("SELECT SUM(pd.actualQuantity) FROM ProductionData pd " +
            "WHERE pd.equipment.id = :equipmentId " +
            "AND pd.timestamp BETWEEN :startTime AND :endTime")
    Integer getTotalProduction(@Param("equipmentId") Long equipmentId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    Page<ProductionData> findByLotNumber(String lotNumber, Pageable pageable);
}