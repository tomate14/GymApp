package com.fitexa.repository;

import com.fitexa.entities.ExercisePersonalRecordEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExercisePersonalRecordRepository extends JpaRepository<ExercisePersonalRecordEntity, Long> {
    List<ExercisePersonalRecordEntity> findByUserId(Long userId);
    Optional<ExercisePersonalRecordEntity> findByUserIdAndExerciseId(Long userId, Long exerciseId);
}
