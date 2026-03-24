package com.fitexa.repository;

import java.time.LocalDate;

public interface DashboardClassSubscriptionCountProjection {
    Long getGymClassId();
    LocalDate getTargetDate();
    Long getSubscribedCount();
}
