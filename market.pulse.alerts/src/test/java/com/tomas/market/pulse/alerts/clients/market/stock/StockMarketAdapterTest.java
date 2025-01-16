package com.tomas.market.pulse.alerts.clients.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.stock.profit.ProfitApiClient;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.StockProfitDTO;
import com.tomas.market.pulse.alerts.model.Stock;

import reactor.core.publisher.Mono;

//TODO ver que onda esta anotacion
@MockitoSettings(strictness = Strictness.LENIENT)
public class StockMarketAdapterTest {
  @InjectMocks
  private StockMarketAdapter stockMarketAdapter;
  @Mock
  private ProfitApiClient profitApiClient;

  @Test
  void shouldFetchMarketDataAndMapToStockList() {
    List<Stock> expectedStocks = List.of(new Stock("symbol1", "name1", 0), new Stock("symbol2", "name2", 1));

    when(profitApiClient.fetchStocksData()).thenReturn(Mono.just(List.of(new StockProfitDTO("symbol1", "name1", 0d), new StockProfitDTO("symbol2", "name2", 1d))));

    Mono<List<Stock>> actualMono = stockMarketAdapter.fetchMarketData();

    assertEquals(expectedStocks, actualMono.block());
  }

  @Test
  void shouldFetchByIdAndMapToStock(){
    String symbol = "symbol1";
    Stock expectedStock = new Stock(symbol, "name1", 0);
    when(profitApiClient.fetchStockById(symbol)).thenReturn(Mono.just(new StockProfitDTO(symbol, "name1", 0d)));

    var actualMono = stockMarketAdapter.fetchById(symbol);

    assertEquals(expectedStock, actualMono.block());
  }

  @Test
  void shouldThrowExceptionWhileFetchingByIdIfItDoesNotExist(){
    String symbol = "symbol1";

    when(profitApiClient.fetchStockById(symbol)).thenReturn(Mono.just(new StockProfitDTO(null, null, 0)));

    var e = assertThrows(ResponseStatusException.class, () -> stockMarketAdapter.fetchById(symbol).block());
    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }
}
