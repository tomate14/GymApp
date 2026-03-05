package com.fitexa.services;

import com.fitexa.entities.RoutineAssignmentEntity;
import com.fitexa.entities.UserEntity;
import java.util.List;
import java.util.Optional;

public interface RoutinePlanValidationStrategy {
    Optional<String> validate(UserEntity user, RoutineAssignmentEntity candidate, List<RoutineAssignmentEntity> overlaps);
}
