package com.tomas.market.pulse.alerts.api.dtos;

import java.util.List;

import com.tomas.market.pulse.alerts.model.FinancialInstrument;

public record FinancialInstrumentResponse(
  //TODO deberia ser un Map para que tenga sentido el generico
  //private Map<MarketType, ? extends FinancialInstrument> instruments;
  List<? extends FinancialInstrument> crypto,
  List<? extends FinancialInstrument> stock
){}
