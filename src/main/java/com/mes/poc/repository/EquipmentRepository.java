package com.mes.poc.repository;

import com.mes.poc.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByEquipmentCode(String equipmentCode);

    List<Equipment> findByStatus(Equipment.EquipmentStatus status);

    @Query("SELECT e FROM Equipment e WHERE e.location = :location")
    List<Equipment> findByLocation(@Param("location") String location);
}