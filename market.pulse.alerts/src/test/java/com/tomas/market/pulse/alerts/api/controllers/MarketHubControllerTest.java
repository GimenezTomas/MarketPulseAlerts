package com.tomas.market.pulse.alerts.api.controllers;

import com.tomas.market.pulse.alerts.api.services.MarketHubService;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.Stock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

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
    /*CryptoCurrency crypto = new CryptoCurrency("BTC", "Bitcoin", 123);
    Stock stock = new Stock("AAPL", "Apple", 1234);
    List<FinancialInstrument> instruments = List.of(crypto, stock);
    when(marketHubService.getAll()).thenReturn(instruments);

    webTestClient.get()
        .uri("/api/markets")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(FinancialInstrument.class)
        .isEqualTo(instruments);

    Mockito.verify(marketHubService).getAll();
    todo
    */
  }
}
