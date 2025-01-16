package com.tomas.market.pulse.alerts.api.controllers;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.api.dtos.SubscriptionRequest;
import com.tomas.market.pulse.alerts.api.services.MarketHubService;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.Stock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class MarketHubControllerTest {

  @InjectMocks
  private MarketHubController marketHubController;
  @Mock
  private MarketHubService marketHubService;
//TODO chequear si esto asi esta ok
  private WebTestClient webTestClient;

  @BeforeEach
  void beforeAll(){
    webTestClient = WebTestClient.bindToController(marketHubController).build();
  }

  @Test
  void shouldReturnAllFinancialInstruments() {
    CryptoCurrency crypto = new CryptoCurrency("BTC", "Bitcoin", 123);
    Stock stock = new Stock("AAPL", "Apple", 1234);
    FinancialInstrumentResponse financialInstrumentResponse = new FinancialInstrumentResponse(List.of(crypto), List.of(stock));

    when(marketHubService.getAll()).thenReturn(financialInstrumentResponse);

    webTestClient.get()
        .uri("/api/markets")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(FinancialInstrumentResponse.class)
        .isEqualTo(financialInstrumentResponse);

    Mockito.verify(marketHubService).getAll();
  }
/*

  @Test
  void shouldCreateSubscriptionSuccessfully() {
    String subscriptionId = "12345";
    SubscriptionRequest request = new SubscriptionRequest(
        "financialInstrumentId123",
        "user@example.com",
        100,
        50,
        200.0,
        150.0,
        MarketType.CRYPTO
    );

    doThrow(marketHubService.subscribeUserToFinancialInstrument(request.email(), request.financialInstrumentId(), request.marketType(), request.upperThreshold(), request.lowerThreshold()));

    // Act & Assert
    webTestClient.post()
        .uri("/api/subscriptions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.message").isEqualTo("Subscription created successfully.")
        .jsonPath("$.subscriptionId").isEqualTo(subscriptionId);
  }

  @Test
  void shouldReturnConflictWhenAlreadySubscribed() {
    // Arrange
    SubscriptionRequest request = new SubscriptionRequest(
        "financialInstrumentId123",
        "user@example.com",
        100,
        50,
        200.0,
        150.0,
        MarketType.CRYPTO
    );

    doThrow(new ResponseStatusException(HttpStatus.CONFLICT, ""), () -> marketHubService.subscribeUserToFinancialInstrument(request.email(), request.financialInstrumentId(), request.marketType(), request.upperThreshold(), request.lowerThreshold()));

    // Act & Assert
    webTestClient.post()
        .uri("/api/subscriptions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isConflict()
        .expectBody()
        .jsonPath("$.message").isEqualTo("You are already subscribed to this financial instrument.");
  }

  @Test
  void shouldReturnNotFoundWhenFinancialInstrumentDoesNotExist() {
    // Arrange
    SubscriptionRequest request = new SubscriptionRequest(
        "financialInstrumentId123",
        "user@example.com",
        100,
        50,
        200.0,
        150.0,
        MarketType.CRYPTO
    );
    when(subscriptionService.subscribe(any())).thenReturn(Mono.error(new FinancialInstrumentNotFoundException()));

    // Act & Assert
    webTestClient.post()
        .uri("/api/subscriptions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.message").isEqualTo("Financial instrument not found.");
  }*/
}
