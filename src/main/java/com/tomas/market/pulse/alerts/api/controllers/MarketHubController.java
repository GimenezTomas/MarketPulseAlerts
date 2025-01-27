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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@Validated
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarketHubController {

  private final MarketHubService marketHubService;

  @Operation(summary = "Retrieve all financial instruments",
      description = "This endpoint returns a list of all financial instruments available in the system, excluding detailed information from our database.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Financial instruments retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/markets")
  public FinancialInstrumentResponse getAllFinancialInstruments() {
    return marketHubService.getAll();
  }

  @Operation(summary = "Subscribe to a financial instrument")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  @PostMapping("/subscriptions")
  public ResponseEntity<String> subscribe(@Valid @RequestBody SubscriptionRequest request) {
    marketHubService.subscribeUserToFinancialInstrument(request.email(), request.financialInstrumentId(), request.marketType(),
        request.upperThreshold(), request.lowerThreshold());
    return new ResponseEntity<>("Subscription created successfully.", HttpStatus.CREATED);
  }

  @Operation(summary = "Unsubscribe from a financial instrument")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Subscription canceled successfully"),
      @ApiResponse(responseCode = "404", description = "Subscription not found")
  })
  @DeleteMapping("/subscriptions")
  public ResponseEntity<Void> unsubscribeUser(
      @RequestParam String email,
      @RequestParam String financialInstrumentId) {
    marketHubService.unSubscribeUserFromFinancialInstrumentNotifications(email, financialInstrumentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Synchronize financial instruments",
      description = "This endpoint checks external APIs for new financial instruments that should be added to the database.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/markets/sync")
  public ResponseEntity<String> syncFinancialInstruments() {
    marketHubService.syncNewFinancialInstruments();
    return ResponseEntity.ok("Financial instruments synchronization completed successfully.");
  }

  @Operation(summary = "Get financial instruments by email",
      description = "This endpoint retrieves all financial instruments to which the specified email is subscribed, including detailed information such as current prices.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Financial instruments retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Email not found")
  })
  @GetMapping("/subscriptions/{email}/financial-instruments")
  public ResponseEntity<FinancialInstrumentResponse> getFinancialInstrumentsByEmail(
      @PathVariable String email) {
    FinancialInstrumentResponse financialInstruments = marketHubService.getSubscribedFinancialInstrumentsByUser(email);
    return new ResponseEntity<>(financialInstruments, HttpStatus.OK);
  }
}