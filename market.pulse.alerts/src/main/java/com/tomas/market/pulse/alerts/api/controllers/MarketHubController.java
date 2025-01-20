package com.tomas.market.pulse.alerts.api.controllers;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.api.dtos.SubscriptionRequest;
import com.tomas.market.pulse.alerts.api.services.MarketHubService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketHubController {

  private final MarketHubService marketHubService;

  @GetMapping
  public FinancialInstrumentResponse getAllFinancialInstruments() {
    return marketHubService.getAll();
  }

  @PostMapping("/subscriptions")
  public ResponseEntity<String> subscribe(@Valid @RequestBody SubscriptionRequest request) {
    marketHubService.subscribeUserToFinancialInstrument(request.email(), request.financialInstrumentId(), request.marketType(),
        request.upperThreshold(), request.lowerThreshold());
    return new ResponseEntity<>("Subscription created successfully.", HttpStatus.CREATED);
  }

  @DeleteMapping("/subscriptions")
  public ResponseEntity<Void> unsubscribeUser(
      @RequestParam String email,
      @RequestParam String financialInstrumentId) {

    marketHubService.unSubscribeUserFromFinancialInstrumentNotifications(email, financialInstrumentId);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/sync")
  public ResponseEntity<String> syncFinancialInstruments(){
    marketHubService.syncNewFinancialInstruments();
    return ResponseEntity.ok("Financial instruments synchronization completed successfully.");
  }
}