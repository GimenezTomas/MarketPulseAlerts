package com.tomas.market.pulse.alerts.api.dtos;

import java.util.List;
import java.util.Map;

import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.MarketType;

public record FinancialInstrumentResponse (
    Map<MarketType, List<? extends FinancialInstrument>> instruments
){}
