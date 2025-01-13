package com.tomas.market.pulse.alerts.api.services;

import java.util.List;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;

public interface MarketHubService {
  FinancialInstrumentResponse getAll();
  void subscribeUserToFinancialInstrument(String email, String financialInstrumentId );
}
