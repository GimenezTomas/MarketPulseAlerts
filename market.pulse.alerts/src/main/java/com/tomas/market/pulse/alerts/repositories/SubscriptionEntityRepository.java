package com.tomas.market.pulse.alerts.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tomas.market.pulse.alerts.model.entities.SubscriptionEntity;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, Long> {
  //TODO AGREGAR MARKET TYPE
  boolean existsSubscriptionEntityByFinancialInstrument_SymbolAndEmail(String symbol, String email);
  //TODO AGREGAR MARKET TYPE
  void deleteByFinancialInstrument_SymbolAndEmail(String id, String email);
  List<SubscriptionEntity> findAllByEmail(String email);
}
