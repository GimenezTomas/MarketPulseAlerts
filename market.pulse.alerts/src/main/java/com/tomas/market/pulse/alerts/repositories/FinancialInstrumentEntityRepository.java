package com.tomas.market.pulse.alerts.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

public interface FinancialInstrumentEntityRepository extends JpaRepository<FinancialInstrumentEntity, Long> {
  List<FinancialInstrumentEntity> findAllBySymbolIn(List<String> symbols);
  Optional<FinancialInstrumentEntity> findBySymbolAndMarketType(String symbol, MarketType marketType);
  @Query("SELECT DISTINCT f FROM FinancialInstrumentEntity f JOIN SubscriptionEntity s ON s.financialInstrument = f")
  List<FinancialInstrumentEntity> findAllWithRelatedSubscriptions();
}
