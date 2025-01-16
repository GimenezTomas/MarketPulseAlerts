package com.tomas.market.pulse.alerts.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.tomas.market.pulse.alerts.model.SubscriptionEntity;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, Long> {
  boolean existsSubscriptionEntityByFinancialInstrumentIdAndEmail(String id, String email);
  void deleteByFinancialInstrumentIdAndEmail(String id, String email);
  List<SubscriptionEntity> findAllByEmail(String email);
}
