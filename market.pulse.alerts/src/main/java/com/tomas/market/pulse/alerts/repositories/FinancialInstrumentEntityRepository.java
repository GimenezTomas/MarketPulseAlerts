package com.tomas.market.pulse.alerts.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

public interface FinancialInstrumentEntityRepository extends JpaRepository<FinancialInstrumentEntity, Long> {

  List<FinancialInstrumentEntity> findAllBySymbolIn(List<String> symbols);
}
