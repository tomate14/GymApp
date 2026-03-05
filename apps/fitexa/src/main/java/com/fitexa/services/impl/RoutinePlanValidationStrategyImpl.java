package com.fitexa.services.impl;

import com.fitexa.entities.PlanEntity;
import com.fitexa.entities.RoutineAssignmentEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.entities.WeekDay;
import com.fitexa.services.RoutinePlanValidationStrategy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RoutinePlanValidationStrategyImpl implements RoutinePlanValidationStrategy {

    @Override
    public Optional<String> validate(UserEntity user, RoutineAssignmentEntity candidate, List<RoutineAssignmentEntity> overlaps) {
        if (user.getUserType() != UserType.ALUMNO) {
            return Optional.empty();
        }

        PlanEntity plan = user.getPlan();
        if (plan == null) {
            return Optional.of("Warning: el alumno no tiene plan asignado.");
        }

        Set<WeekDay> uniqueDays = new LinkedHashSet<>();
        overlaps.forEach(item -> uniqueDays.addAll(item.getDays()));
        uniqueDays.addAll(candidate.getDays());

        if (uniqueDays.size() > plan.getMaxDaysPerWeek()) {
            return Optional.of(
                "Warning: el alumno tiene el plan %s con maximo de %d dias semanales."
                    .formatted(plan.getName(), plan.getMaxDaysPerWeek())
            );
        }

        return Optional.empty();
    }
}
