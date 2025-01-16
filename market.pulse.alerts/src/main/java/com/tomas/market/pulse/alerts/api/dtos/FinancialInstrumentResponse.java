package com.tomas.market.pulse.alerts.api.dtos;

import java.util.List;
import java.util.Map;

import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.MarketType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record FinancialInstrumentResponse(
  //TODO deberia ser un Map para que tenga sentido el generico
  //private Map<MarketType, ? extends FinancialInstrument> instruments;
  List<? extends FinancialInstrument> crypto,
  List<? extends FinancialInstrument> stock
){}
