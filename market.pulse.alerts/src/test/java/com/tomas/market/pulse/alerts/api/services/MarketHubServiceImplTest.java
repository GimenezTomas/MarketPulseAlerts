package com.tomas.market.pulse.alerts.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.clients.market.MarketDataAdapter;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.Stock;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;
import com.tomas.market.pulse.alerts.model.entities.SubscriptionEntity;
import com.tomas.market.pulse.alerts.repositories.FinancialInstrumentEntityRepository;
import com.tomas.market.pulse.alerts.repositories.SubscriptionEntityRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Mono;

class MarketHubServiceImplTest {
  private MarketHubServiceImpl marketHubService;
  @Mock
  private MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter;
  @Mock
  private MarketDataAdapter<Stock> stockMarketAdapter;
  @Mock
  private SubscriptionEntityRepository subscriptionRepository;
  @Mock
  private FinancialInstrumentEntityRepository financialInstrumentRepository;
  private CryptoCurrency cryptoCurrency1;
  private CryptoCurrency cryptoCurrency2;
  private Stock stock1;
  private Stock stock2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    marketHubService = new MarketHubServiceImpl(cryptoMarketAdapter, stockMarketAdapter, subscriptionRepository, financialInstrumentRepository);

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

    assertEquals(0, financialInstrumentList.crypto().size());
    assertEquals(0, financialInstrumentList.stock().size());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithOneElementWhenOneCryptoCurrencyIsAvailable() {
    List<FinancialInstrumentEntity> cryptoCurrencies = List.of(FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency1.getSymbol())
        .name(cryptoCurrency1.getName())
        .marketType(MarketType.CRYPTO)
        .build());
    var expectedCryptoCurrency = cryptoCurrency1;
    expectedCryptoCurrency.setPrice(0);

    when(financialInstrumentRepository.findAll()).thenReturn(cryptoCurrencies);
    FinancialInstrumentResponse financialInstrumentList = marketHubService.getAll();

    assertEqualsListFinancialInstruments(List.of(expectedCryptoCurrency), financialInstrumentList.crypto());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithTwoElementsWhenTwoCurrenciesAreAvailable(){
    List<FinancialInstrumentEntity> cryptoCurrencies = List.of(FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency1.getSymbol())
        .name(cryptoCurrency1.getName())
        .marketType(MarketType.CRYPTO)
        .build(),
      FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency2.getSymbol())
        .name(cryptoCurrency2.getName())
        .marketType(MarketType.CRYPTO)
        .build()
    );

    var expectedCryptoCurrencies = List.of(cryptoCurrency1, cryptoCurrency2);
    expectedCryptoCurrencies.forEach(c -> c.setPrice(0));

    when(financialInstrumentRepository.findAll()).thenReturn(cryptoCurrencies);
    FinancialInstrumentResponse financialInstrumentList = marketHubService.getAll();

    assertEqualsListFinancialInstruments(expectedCryptoCurrencies, financialInstrumentList.crypto());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithTwoElementsWhenTwoStocksAreAvailable(){
    List<FinancialInstrumentEntity> stocks = List.of(FinancialInstrumentEntity.builder()
            .symbol(stock1.getSymbol())
            .name(stock1.getName())
            .marketType(MarketType.STOCK)
            .build(),
        FinancialInstrumentEntity.builder()
            .symbol(stock2.getSymbol())
            .name(stock2.getName())
            .marketType(MarketType.STOCK)
            .build()
    );

    var expectedStocks = List.of(stock1, stock2);
    expectedStocks.forEach(s -> s.setPrice(0));

    when(financialInstrumentRepository.findAll()).thenReturn(stocks);
    FinancialInstrumentResponse financialInstrumentList = marketHubService.getAll();

    assertEqualsListFinancialInstruments(expectedStocks, financialInstrumentList.stock());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWhenCryptoAndStocksAreAvailable() {
    List<FinancialInstrumentEntity> instruments = List.of(FinancialInstrumentEntity.builder()
            .symbol(cryptoCurrency1.getSymbol())
            .name(cryptoCurrency1.getName())
            .marketType(MarketType.CRYPTO)
            .build(),
        FinancialInstrumentEntity.builder()
            .symbol(cryptoCurrency2.getSymbol())
            .name(cryptoCurrency2.getName())
            .marketType(MarketType.CRYPTO)
            .build(),
        FinancialInstrumentEntity.builder()
            .symbol(stock1.getSymbol())
            .name(stock1.getName())
            .marketType(MarketType.STOCK)
            .build(),
        FinancialInstrumentEntity.builder()
            .symbol(stock2.getSymbol())
            .name(stock2.getName())
            .marketType(MarketType.STOCK)
            .build()
    );

    var expectedCryptoCurrencies = List.of(cryptoCurrency1, cryptoCurrency2);
    expectedCryptoCurrencies.forEach(c -> c.setPrice(0));

    var expectedStocks = List.of(stock1, stock2);
    expectedStocks.forEach(s -> s.setPrice(0));

    when(financialInstrumentRepository.findAll()).thenReturn(instruments);
    FinancialInstrumentResponse financialInstrumentResponse = marketHubService.getAll();

    assertEqualsListFinancialInstruments(List.of(cryptoCurrency1, cryptoCurrency2), financialInstrumentResponse.crypto());
    assertEqualsListFinancialInstruments(List.of(stock1, stock2), financialInstrumentResponse.stock());
  }
  
  @Test
  void shouldAllowUserSubscribeToCryptoCurrencyNotificationsWhenItExists(){
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.CRYPTO;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(false);
    when(cryptoMarketAdapter.fetchById(cod)).thenReturn(Mono.just(cryptoCurrency1));

    marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold);

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrumentIdAndEmail(cod, email);
    verify(subscriptionRepository).save(any());
    verify(cryptoMarketAdapter).fetchById(cod);
    verifyNoInteractions(stockMarketAdapter);
  }

  @Test
  void shouldNotAllowSubscriptionToCryptoCurrencyNotificationsWhenDoesNotExist(){
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.CRYPTO;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(false);
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crypto currency not found")).when(cryptoMarketAdapter).fetchById(cod);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold));

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrumentIdAndEmail(cod, email);
    verify(cryptoMarketAdapter).fetchById(cod);
    verifyNoInteractions(stockMarketAdapter);

    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  @Test
  void shouldThrowExceptionWhenUserIsAlreadySubscribedToTheFinancialInstrumentNotifications(){
    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(true);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> marketHubService.subscribeUserToFinancialInstrument("", "", MarketType.CRYPTO, 0,0));

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString());
    verifyNoInteractions(cryptoMarketAdapter);
    verifyNoInteractions(stockMarketAdapter);

    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
  }

  @Test
  void shouldAllowUserSubscribeToStockNotificationsWhenItExists() {
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.STOCK;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(false);
    when(stockMarketAdapter.fetchById(cod)).thenReturn(Mono.just(stock1));

    marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold);

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrumentIdAndEmail(cod, email);
    verify(subscriptionRepository).save(any());
    verify(stockMarketAdapter).fetchById(cod);
    verifyNoInteractions(cryptoMarketAdapter);
  }

  @Test
  void shouldNotAllowSubscriptionToStockNotificationsWhenDoesNotExist() {
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.STOCK;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(anyString(), anyString())).thenReturn(false);
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found")).when(stockMarketAdapter).fetchById(cod);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () ->
        marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold)
    );

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrumentIdAndEmail(cod, email);
    verify(subscriptionRepository, times(0)).save(any());
    verify(stockMarketAdapter).fetchById(cod);
    verifyNoInteractions(cryptoMarketAdapter);

    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    assertEquals("Stock not found", e.getReason());
  }

  @Test
  void shouldAllowUserToUnsubscribeFromFinancialInstrumentNotificationsWhenUserHasAnOngoingSubscription(){
    String cod = "cod1";
    String email = "email@gmail.com";

    marketHubService.unSubscribeUserFromFinancialInstrumentNotifications(email, cod);

    verify(subscriptionRepository, times(1)).deleteByFinancialInstrumentIdAndEmail(cod, email);
  }

  @Test
  void shouldPersistAllCryptoCurrenciesWhenFinancialInstrumentsTableIsEmpty() {
    List<CryptoCurrency> cryptoCurrencies = List.of(cryptoCurrency1);
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(cryptoCurrencies));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(financialInstrumentRepository.findAllBySymbolIn(cryptoCurrencies.stream()
        .map(CryptoCurrency::getSymbol)
        .toList()))
        .thenReturn(new ArrayList<>());

    marketHubService.syncNewFinancialInstruments();

    verify(financialInstrumentRepository).findAllBySymbolIn(cryptoCurrencies.stream()
        .map(CryptoCurrency::getSymbol)
        .toList());

    verify(financialInstrumentRepository).saveAll(argThat(savedIterable -> {
      List<?> savedList = StreamSupport.stream(savedIterable.spliterator(), false).toList();
      return savedList.size() == cryptoCurrencies.size() &&
          savedList.stream().allMatch(item -> cryptoCurrencies.stream()
              .anyMatch(crypto -> crypto.getSymbol().equals(((FinancialInstrumentEntity) item).getSymbol())));
    }));

    verify(cryptoMarketAdapter).fetchMarketData();
    verifyNoMoreInteractions(financialInstrumentRepository, cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldPersistAllStocksWhenFinancialInstrumentsTableIsEmpty() {
    List<Stock> stocks = List.of(stock1);
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(stocks));
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(financialInstrumentRepository.findAllBySymbolIn(stocks.stream()
        .map(Stock::getSymbol)
        .toList()))
        .thenReturn(new ArrayList<>());

    marketHubService.syncNewFinancialInstruments();

    verify(financialInstrumentRepository).findAllBySymbolIn(stocks.stream()
        .map(Stock::getSymbol)
        .toList());

    verify(financialInstrumentRepository).saveAll(argThat(savedIterable -> {
      List<?> savedList = StreamSupport.stream(savedIterable.spliterator(), false).toList();
      return savedList.size() == stocks.size() &&
          savedList.stream().allMatch(item -> stocks.stream()
              .anyMatch(stock -> stock.getSymbol().equals(((FinancialInstrumentEntity) item).getSymbol())));
    }));

    verify(stockMarketAdapter).fetchMarketData();
    verify(cryptoMarketAdapter).fetchMarketData();
    verifyNoMoreInteractions(financialInstrumentRepository, cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldNotPersistExistingCryptoCurrencies() {
    List<CryptoCurrency> cryptoCurrencies = List.of(cryptoCurrency1, cryptoCurrency2);
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(cryptoCurrencies));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(financialInstrumentRepository.findAllBySymbolIn(cryptoCurrencies.stream()
        .map(CryptoCurrency::getSymbol)
        .toList()))
        .thenReturn(List.of(FinancialInstrumentEntity.builder().symbol(cryptoCurrency2.getSymbol()).build()));

    marketHubService.syncNewFinancialInstruments();

    verify(financialInstrumentRepository).findAllBySymbolIn(cryptoCurrencies.stream()
        .map(CryptoCurrency::getSymbol)
        .toList());

    verify(financialInstrumentRepository).saveAll(argThat(savedIterable -> {
      List<?> savedList = StreamSupport.stream(savedIterable.spliterator(), false).toList();
      return savedList.size() == 1 &&
          savedList.stream().allMatch(item -> item instanceof FinancialInstrumentEntity &&
              cryptoCurrency1.getSymbol().equals(((FinancialInstrumentEntity) item).getSymbol()));
    }));

    verify(cryptoMarketAdapter).fetchMarketData();
    verify(stockMarketAdapter).fetchMarketData();
    verifyNoMoreInteractions(financialInstrumentRepository, cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldNotPersistExistingStocks() {
    List<Stock> stocks = List.of(stock1, stock2);
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(stocks));
    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(financialInstrumentRepository.findAllBySymbolIn(stocks.stream()
        .map(Stock::getSymbol)
        .toList()))
        .thenReturn(List.of(FinancialInstrumentEntity.builder().symbol(stock2.getSymbol()).build()));

    marketHubService.syncNewFinancialInstruments();

    verify(financialInstrumentRepository).findAllBySymbolIn(stocks.stream()
        .map(Stock::getSymbol)
        .toList());

    verify(financialInstrumentRepository).saveAll(argThat(savedIterable -> {
      List<?> savedList = StreamSupport.stream(savedIterable.spliterator(), false).toList();
      return savedList.size() == 1 &&
          savedList.stream().allMatch(item -> item instanceof FinancialInstrumentEntity &&
              stock1.getSymbol().equals(((FinancialInstrumentEntity) item).getSymbol()));
    }));

    verify(stockMarketAdapter).fetchMarketData();
    verify(cryptoMarketAdapter).fetchMarketData();
    verifyNoMoreInteractions(financialInstrumentRepository, cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoSubscription(){
    String email = "email@gmail.com";
    List<MarketType> markets = List.of(MarketType.CRYPTO, MarketType.STOCK);

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(new ArrayList<>());
    FinancialInstrumentResponse response = marketHubService.getSubscribedFinancialInstrumentsByUserAndMarketTypes(email, markets);

    assertEquals(0, response.stock().size());
    assertEquals(0, response.crypto().size());
  }

  @Test
  void shouldReturnOneElementListWhenUserHasOnlyOneCryptoCurrencySubscription(){
    String email = "email@gmail.com";
    List<MarketType> markets = List.of(MarketType.CRYPTO);
    SubscriptionEntity expectedSubscription = SubscriptionEntity.builder().financialInstrumentId(cryptoCurrency1.getSymbol()).build();

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(List.of(expectedSubscription));
    FinancialInstrumentResponse response = marketHubService.getSubscribedFinancialInstrumentsByUserAndMarketTypes(email, markets);

    assertEquals(0, response.stock().size());
    assertEquals(1, response.crypto().size());
    assertEquals(List.of(expectedSubscription.getFinancialInstrumentId()), response.crypto().get(0).getSymbol());
  }
  /*@Test
  void shouldReturnOneElementListWhenUserHasOnlyOneCryptoCurrencySubscription(){
    String email = "email@gmail.com";
    List<MarketType> markets = List.of(MarketType.CRYPTO, MarketType.STOCK);
    List<SubscriptionEntity> expectedSubscriptions = List.of(
        SubscriptionEntity.builder().financialInstrumentId(cryptoCurrency1.getSymbol()).build(),
        SubscriptionEntity.builder().financialInstrumentId(stock1.getSymbol()).build()
    );

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(expectedSubscriptions);
    marketHubService.getSubscribedFinancialInstrumentsByUserAndMarketTypes(email, markets);

  }*/

  private void assertEqualsListFinancialInstruments(List<? extends FinancialInstrument> expected, List<? extends FinancialInstrument> current){
    assertEquals(expected.size(), current.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).getSymbol(), current.get(i).getSymbol());
      assertEquals(expected.get(i).getName(), current.get(i).getName());
      assertEquals(expected.get(i).getPrice(), current.get(i).getPrice(), 0.01);
    }
  }
}