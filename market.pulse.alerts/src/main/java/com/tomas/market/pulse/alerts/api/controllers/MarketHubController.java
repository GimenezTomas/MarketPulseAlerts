package com.tomas.market.pulse.alerts.api.controllers;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.api.services.MarketHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketHubController {

  private final MarketHubService marketHubService;

  @GetMapping
  public FinancialInstrumentResponse getAllFinancialInstruments() {
    return marketHubService.getAll();
  }
}