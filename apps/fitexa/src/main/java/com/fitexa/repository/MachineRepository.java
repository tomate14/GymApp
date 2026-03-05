package com.fitexa.repository;

import com.fitexa.entities.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {
}
