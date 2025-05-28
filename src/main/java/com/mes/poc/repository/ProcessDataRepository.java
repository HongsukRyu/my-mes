package com.mes.poc.repository;

import com.mes.poc.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDataRepository extends JpaRepository<ProcessData, Long> {

    List<ProcessData> findByEquipmentAndParameterNameOrderByTimestampDesc(
            Equipment equipment, String parameterName);

    @Query("SELECT pd FROM ProcessData pd WHERE pd.equipment.id = :equipmentId " +
            "AND pd.timestamp BETWEEN :startTime AND :endTime")
    List<ProcessData> findByEquipmentAndTimeRange(
            @Param("equipmentId") Long equipmentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT pd FROM ProcessData pd WHERE pd.nodeId = :nodeId " +
            "ORDER BY pd.timestamp DESC LIMIT 1")
    Optional<ProcessData> findLatestByNodeId(@Param("nodeId") String nodeId);
}