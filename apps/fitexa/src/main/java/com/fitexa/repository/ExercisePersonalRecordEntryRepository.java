package com.fitexa.repository;

import com.fitexa.entities.ExercisePersonalRecordEntryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExercisePersonalRecordEntryRepository extends JpaRepository<ExercisePersonalRecordEntryEntity, Long> {
    List<ExercisePersonalRecordEntryEntity> findByUserIdAndExerciseIdOrderByRecordedAtAscIdAsc(Long userId, Long exerciseId);
}
