package com.tomas.market.pulse.alerts.clients.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.stock.profit.ProfitApiClient;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.StockProfitDTO;
import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.Stock;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class StockMarketAdapterTest {
  @InjectMocks
  private StockMarketAdapter stockMarketAdapter;
  @Mock
  private ProfitApiClient profitApiClient;

  @Test
  void shouldFetchMarketDataAndMapToStockList() {
    List<Stock> expectedStocks = List.of(new Stock("symbol1", "name1", 0), new Stock("symbol2", "name2", 1));

    when(profitApiClient.fetchStocksData()).thenReturn(Mono.just(List.of(new StockProfitDTO("symbol1", "name1", 0d, "", ""), new StockProfitDTO("symbol2", "name2", 1d, "", ""))));

    Mono<List<Stock>> actualMono = stockMarketAdapter.fetchMarketData();

    assertEquals(expectedStocks, actualMono.block());
  }

  @Test
  void shouldFetchByIdAndMapToStock(){
    var financialInstrumentEntity = FinancialInstrumentEntity.builder()
        .symbol("symbol1")
        .name("name1")
        .marketType(MarketType.STOCK)
        .build();

    Stock expectedStock = new Stock(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getName(), 0, "", "");
    when(profitApiClient.fetchStockById(financialInstrumentEntity.getSymbol())).thenReturn(Mono.just(new StockProfitDTO(financialInstrumentEntity.getSymbol(), "name1", 0d, "", "")));

    var actualMono = stockMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity);

    assertEquals(expectedStock, actualMono.block());
  }

  @Test
  void shouldThrowExceptionWhileFetchingByIdIfItDoesNotExist(){
    var financialInstrumentEntity = FinancialInstrumentEntity.builder()
        .symbol("symbol1")
        .name("name1")
        .marketType(MarketType.CRYPTO)
        .build();

    when(profitApiClient.fetchStockById(financialInstrumentEntity.getSymbol())).thenReturn(Mono.just(new StockProfitDTO(null, null, 0, null, null)));

    var monoResult = stockMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity);
    var e = assertThrows(ResponseStatusException.class, monoResult::block);
    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  @Test
  void shouldFetchByIdsAndMapToStocksList(){
    var stock1 = new StockProfitDTO("s1", "n1", 900, "", "");
    var stock2 = new StockProfitDTO("s2", "n2", 700, "", "");

    when(profitApiClient.fetchStockById(stock1.name())).thenReturn(Mono.just(stock1));
    when(profitApiClient.fetchStockById(stock2.name())).thenReturn(Mono.just(stock2));

    Mono<List<Stock>> response = stockMarketAdapter.fetchByIds(List.of(stock1.name(), stock2.name()));
    var stockList = response.block();

    assertEquals(2, Objects.requireNonNull(stockList).size());

    assertEquals(stock1.symbol(), stockList.get(0).getSymbol());
    assertEquals(stock1.name(), stockList.get(0).getName());
    assertEquals(stock1.price(), stockList.get(0).getPrice());

    assertEquals(stock2.symbol(), stockList.get(1).getSymbol());
    assertEquals(stock2.name(), stockList.get(1).getName());
    assertEquals(stock2.price(), stockList.get(1).getPrice());
  }

  @Test
  void shouldFetchByIdsAndMapToStockOneElementListWhenOnlyOneElementWasFounded(){
    var stock1 = new StockProfitDTO("s1", "n1", 900, "", "");
    var stock2 = new StockProfitDTO("s2", "n2", 700, "", "");

    when(profitApiClient.fetchStockById(stock1.name())).thenReturn(Mono.just(stock1));
    when(profitApiClient.fetchStockById(stock2.name())).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

    Mono<List<Stock>> response = stockMarketAdapter.fetchByIds(List.of(stock1.name(), stock2.name()));
    var stockList = response.block();

    assertEquals(1, Objects.requireNonNull(stockList).size());

    assertEquals(stock1.symbol(), stockList.get(0).getSymbol());
    assertEquals(stock1.name(), stockList.get(0).getName());
    assertEquals(stock1.price(), stockList.get(0).getPrice());
  }

  @Test
  void shouldReturnEmptyListWhenIdsListIsEmpty() {
    Mono<List<Stock>> response = stockMarketAdapter.fetchByIds(List.of());
    var stockList = response.block();

    assertNotNull(stockList);
    assertTrue(stockList.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenNoStocksAreFound() {
    var stock1 = new StockProfitDTO("s1", "n1", 900, "", "");
    var stock2 = new StockProfitDTO("s2", "n2", 700, "", "");

    when(profitApiClient.fetchStockById(stock1.name())).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    when(profitApiClient.fetchStockById(stock2.name())).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

    Mono<List<Stock>> response = stockMarketAdapter.fetchByIds(List.of(stock1.name(), stock2.name()));
    var stockList = response.block();

    assertNotNull(stockList);
    assertTrue(stockList.isEmpty());
  }
}
