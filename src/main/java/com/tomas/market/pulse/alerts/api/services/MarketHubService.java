package com.tomas.market.pulse.alerts.api.services;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.model.MarketType;

public interface MarketHubService {
  FinancialInstrumentResponse getAll();
  void subscribeUserToFinancialInstrument(String email, String symbol, MarketType marketType, int upperThreshold, int lowerThreshold);
  void unSubscribeUserFromFinancialInstrumentNotifications(String email, MarketType marketType, String financialInstrumentId);
  FinancialInstrumentResponse getSubscribedFinancialInstrumentsByUser(String email);
  void syncNewFinancialInstruments();
  void notifySubscribers();
}
