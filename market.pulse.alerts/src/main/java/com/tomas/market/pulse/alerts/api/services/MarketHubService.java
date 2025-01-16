package com.tomas.market.pulse.alerts.api.services;

import java.util.List;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.model.MarketType;

public interface MarketHubService {
  FinancialInstrumentResponse getAll();
  void subscribeUserToFinancialInstrument(String email, String financialInstrumentId, MarketType marketType, int upperThreshold, int lowerThreshold);
  void unSubscribeUserFromFinancialInstrumentNotifications(String email, String financialInstrumentId);
  FinancialInstrumentResponse getSubscribedFinancialInstrumentsByUserAndMarketTypes(String email, List<MarketType> markets);
}
