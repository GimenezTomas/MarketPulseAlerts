package com.tomas.market.pulse.alerts.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;
import com.tomas.market.pulse.alerts.model.entities.SubscriptionEntity;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, Long> {
  boolean existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(String symbol, MarketType marketType, String email);
  void deleteByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(String id, MarketType marketType, String email);
  List<SubscriptionEntity> findAllByEmail(String email);
  //TODO AGREGAR PAGINADO
  List<SubscriptionEntity> findAllByFinancialInstrument(FinancialInstrumentEntity financialInstrumentEntity);
}
