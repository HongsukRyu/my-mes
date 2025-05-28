package com.mes.poc.repository;

import com.mes.poc.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByStatusOrderByOccurredAtDesc(Alarm.AlarmStatus status);

    List<Alarm> findByEquipmentAndStatusOrderByOccurredAtDesc(
            Equipment equipment, Alarm.AlarmStatus status);

    @Query("SELECT a FROM Alarm a WHERE a.severity = :severity " +
            "AND a.status = 'ACTIVE' ORDER BY a.occurredAt DESC")
    List<Alarm> findActiveBySeverity(@Param("severity") Alarm.AlarmSeverity severity);

    long countByStatusAndSeverity(Alarm.AlarmStatus status, Alarm.AlarmSeverity severity);
}