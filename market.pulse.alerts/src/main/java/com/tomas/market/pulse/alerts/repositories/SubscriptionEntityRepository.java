package com.tomas.market.pulse.alerts.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.tomas.market.pulse.alerts.model.SubscriptionEntity;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, Long> {
  boolean existsSubscriptionEntityByFinancialInstrumentIdAndEmail(String id, String email);
}
