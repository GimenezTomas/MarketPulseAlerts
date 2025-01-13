package com.tomas.market.pulse.alerts.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.clients.market.crypto.CryptoMarketAdapter;
import com.tomas.market.pulse.alerts.clients.market.stock.StockMarketAdapter;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.Stock;
import com.tomas.market.pulse.alerts.repositories.SubscriptionEntityRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Mono;

@MockitoSettings(strictness = Strictness.LENIENT)
public class MarketHubServiceImplTest {

  @InjectMocks
  private MarketHubServiceImpl marketHubService;
  @Mock
  private CryptoMarketAdapter cryptoMarketAdapter;
  @Mock
  private StockMarketAdapter stockMarketAdapter;
  @Mock
  private SubscriptionEntityRepository subscriptionRepository;
  private CryptoCurrency cryptoCurrency1;
  private CryptoCurrency cryptoCurrency2;
  private Stock stock1;
  private Stock stock2;

  @BeforeEach
  void setUp() {
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));

    cryptoCurrency1 = new CryptoCurrency("BTC", "Bitcoin", 92611);
    cryptoCurrency2 = new CryptoCurrency("ETH", "Ethereum", 611);

    stock1 = new Stock("TSLA", "Tesla", 1234);
    stock2 = new Stock("GLOB", "Globant", 100);
  }

  @Test
  void shouldReturnEmptyListOfFinancialInstrumentsWhenNoInstrumentsAreAvailable() {
    FinancialInstrumentResponse financialInstrumentList = marketHubService.getAll();

    assertEquals(0, financialInstrumentList.getCrypto().size());
    assertEquals(0, financialInstrumentList.getStock().size());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithOneElementWhenOneCryptoCurrencyIsAvailable() {
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = Mono.just(List.of(cryptoCurrency1));
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(cryptoCurrenciesMono);

    FinancialInstrumentResponse financialInstrumentList = marketHubService.getAll();

    assertEqualsListFinancialInstruments2(List.of(cryptoCurrency1), financialInstrumentList.getCrypto());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithTwoElementsWhenTwoCurrenciesAreAvailable(){
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = Mono.just(List.of(cryptoCurrency1, cryptoCurrency2));
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(cryptoCurrenciesMono);

    FinancialInstrumentResponse financialInstrumentResponse = marketHubService.getAll();

    assertEqualsListFinancialInstruments2(List.of(cryptoCurrency1, cryptoCurrency2), financialInstrumentResponse.getCrypto());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithTwoElementsWhenTwoStocksAreAvailable(){
    Mono<List<Stock>> stockListMono = Mono.just(List.of(stock1, stock2));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(stockListMono);

    FinancialInstrumentResponse financialInstrumentResponse = marketHubService.getAll();

    assertEqualsListFinancialInstruments2(List.of(stock1, stock2), financialInstrumentResponse.getStock());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWhenCryptoAndStocksAreAvailable() {
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = Mono.just(List.of(cryptoCurrency1, cryptoCurrency2));
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(cryptoCurrenciesMono);

    Mono<List<Stock>> stockListMono = Mono.just(List.of(stock1, stock2));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(stockListMono);

    FinancialInstrumentResponse financialInstrumentResponse = marketHubService.getAll();

    assertEqualsListFinancialInstruments2(List.of(cryptoCurrency1, cryptoCurrency2), financialInstrumentResponse.getCrypto());
    assertEqualsListFinancialInstruments2(List.of(stock1, stock2), financialInstrumentResponse.getStock());
  }

  /*
  @Test
  void shouldAllowSubscriptionToFinancialInstrumentWhenFinancialInstrumentExists(){
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.CRYPTO;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(false);
    when(cryptoMarketAdapter.fetchById(cod)).thenReturn(Mono.just(cryptoCurrency1));

    marketHubService.subscribeUserTo



  }*/

  private void assertEqualsListFinancialInstruments2(List<? extends FinancialInstrument> expected, List<? extends FinancialInstrument> current){
    assertEquals(expected.size(), current.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).getSymbol(), current.get(i).getSymbol());
      assertEquals(expected.get(i).getName(), current.get(i).getName());
      assertEquals(expected.get(i).getPrice(), current.get(i).getPrice(), 0.01);
    }
  }
}