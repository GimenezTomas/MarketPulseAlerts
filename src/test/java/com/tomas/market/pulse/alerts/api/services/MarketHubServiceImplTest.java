package com.tomas.market.pulse.alerts.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
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
  @Mock
  private NotificationService notificationService;
  private CryptoCurrency cryptoCurrency1;
  private CryptoCurrency cryptoCurrency2;
  private Stock stock1;
  private Stock stock2;
  private FinancialInstrumentEntity financialInstrumentEntity;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    marketHubService = new MarketHubServiceImpl(cryptoMarketAdapter, stockMarketAdapter, subscriptionRepository, financialInstrumentRepository, notificationService);

    when(cryptoMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));
    when(stockMarketAdapter.fetchMarketData()).thenReturn(Mono.just(new ArrayList<>()));

    cryptoCurrency1 = new CryptoCurrency("BTC", "Bitcoin", 92611);
    cryptoCurrency2 = new CryptoCurrency("ETH", "Ethereum", 611);

    stock1 = new Stock("TSLA", "Tesla", 1234);
    stock2 = new Stock("GLOB", "Globant", 100);

    financialInstrumentEntity = FinancialInstrumentEntity.builder()
        .symbol("BTC")
        .name("bitcoin")
        .marketType(MarketType.CRYPTO)
        .build();
  }

  @Test
  void shouldReturnEmptyListOfFinancialInstrumentsWhenNoInstrumentsAreAvailable() {
    FinancialInstrumentResponse financialInstrumentResponse = marketHubService.getAll();

    assertEquals(0, financialInstrumentResponse.instruments().get(MarketType.CRYPTO).size());
    assertEquals(0, financialInstrumentResponse.instruments().get(MarketType.STOCK).size());
  }

  @Test
  void shouldReturnListOfFinancialInstrumentsWithOneElementWhenOneCryptoCurrencyIsAvailable() {
    List<FinancialInstrumentEntity> cryptoCurrencies = List.of(FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency1.getSymbol())
        .name(cryptoCurrency1.getName())
        .marketType(MarketType.CRYPTO)
        .build());
    var expectedCryptoCurrency = cryptoCurrency1;
    expectedCryptoCurrency.setPrice(0d);

    when(financialInstrumentRepository.findAll()).thenReturn(cryptoCurrencies);
    var financialInstrumentMap = marketHubService.getAll().instruments();

    assertEqualsListFinancialInstruments(List.of(expectedCryptoCurrency), financialInstrumentMap.get(MarketType.CRYPTO));
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
    expectedCryptoCurrencies.forEach(c -> c.setPrice(0d));

    when(financialInstrumentRepository.findAll()).thenReturn(cryptoCurrencies);
    var financialInstrumentsMap = marketHubService.getAll().instruments();

    assertEqualsListFinancialInstruments(expectedCryptoCurrencies, financialInstrumentsMap.get(MarketType.CRYPTO));
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
    expectedStocks.forEach(s -> s.setPrice(0d));

    when(financialInstrumentRepository.findAll()).thenReturn(stocks);
    var financialInstrumentsMap = marketHubService.getAll().instruments();

    assertEqualsListFinancialInstruments(expectedStocks, financialInstrumentsMap.get(MarketType.STOCK));
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
    expectedCryptoCurrencies.forEach(c -> c.setPrice(0d));

    var expectedStocks = List.of(stock1, stock2);
    expectedStocks.forEach(s -> s.setPrice(0d));

    when(financialInstrumentRepository.findAll()).thenReturn(instruments);
    var financialInstrumentResponse = marketHubService.getAll().instruments();

    assertEqualsListFinancialInstruments(List.of(cryptoCurrency1, cryptoCurrency2), financialInstrumentResponse.get(MarketType.CRYPTO));
    assertEqualsListFinancialInstruments(List.of(stock1, stock2), financialInstrumentResponse.get(MarketType.STOCK));
  }

  @Test
  void shouldAllowUserSubscribeToCryptoCurrencyNotificationsWhenItExists(){
    String email = "email@gmail.com";
    int upperThreshold = 10;
    int lowerThreshold = 10;
    financialInstrumentEntity.setMarketType(MarketType.CRYPTO);

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(), anyString())).thenReturn(false);
    when(financialInstrumentRepository.findBySymbolAndMarketType(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType())).thenReturn(Optional.of(financialInstrumentEntity));
    when(cryptoMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity)).thenReturn(Mono.just(cryptoCurrency1));

    marketHubService.subscribeUserToFinancialInstrument(email, financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(), upperThreshold, lowerThreshold);

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(),email);
    verify(subscriptionRepository).save(any());
    verify(cryptoMarketAdapter).fetchByFinancialInstrument(financialInstrumentEntity);
    verifyNoInteractions(stockMarketAdapter);
  }

  @Test
  void shouldNotAllowSubscriptionToCryptoCurrencyNotificationsWhenDoesNotExistInCryptoAdapter(){
    String email = "email@gmail.com";
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(),anyString())).thenReturn(false);
    when(financialInstrumentRepository.findBySymbolAndMarketType(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType())).thenReturn(Optional.of(financialInstrumentEntity));
    when(cryptoMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crypto currency not found"));

    Executable subscribeAction = () -> marketHubService.subscribeUserToFinancialInstrument(email, financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(), upperThreshold, lowerThreshold);
    ResponseStatusException e = assertThrows(ResponseStatusException.class, subscribeAction);

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(),email);
    verify(cryptoMarketAdapter).fetchByFinancialInstrument(financialInstrumentEntity);
    verifyNoInteractions(stockMarketAdapter);

    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  @Test
  void shouldThrowExceptionWhenUserIsAlreadySubscribedToTheFinancialInstrumentNotifications(){
    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(), anyString())).thenReturn(true);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> marketHubService.subscribeUserToFinancialInstrument("", "", MarketType.CRYPTO, 0,0));

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(),anyString());
    verifyNoInteractions(cryptoMarketAdapter);
    verifyNoInteractions(stockMarketAdapter);

    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
  }

  @Test
  void shouldAllowUserSubscribeToStockNotificationsWhenItExists() {
    String email = "email@gmail.com";
    int upperThreshold = 10;
    int lowerThreshold = 10;
    financialInstrumentEntity.setMarketType(MarketType.STOCK);

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(), anyString())).thenReturn(false);
    when(financialInstrumentRepository.findBySymbolAndMarketType(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType())).thenReturn(Optional.of(financialInstrumentEntity));
    when(stockMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity)).thenReturn(Mono.just(stock1));

    marketHubService.subscribeUserToFinancialInstrument(email, financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(), upperThreshold, lowerThreshold);

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(financialInstrumentEntity.getSymbol(), financialInstrumentEntity.getMarketType(), email);
    verify(subscriptionRepository).save(any());
    verify(stockMarketAdapter).fetchByFinancialInstrument(financialInstrumentEntity);
    verifyNoInteractions(cryptoMarketAdapter);
  }

  @Test
  void shouldNotAllowSubscriptionToStockNotificationsWhenDoesNotExistInStockAdapter() {
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.STOCK;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(), anyString())).thenReturn(false);
    when(financialInstrumentRepository.findBySymbolAndMarketType(cod, marketType)).thenReturn(Optional.of(financialInstrumentEntity));
    when(stockMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () ->
        marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold)
    );

    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(cod, marketType,email);
    verify(subscriptionRepository, times(0)).save(any());
    verify(stockMarketAdapter).fetchByFinancialInstrument(financialInstrumentEntity);
    verifyNoInteractions(cryptoMarketAdapter);

    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    assertEquals("Stock not found", e.getReason());
  }

  @Test
  void shouldNotAllowSubscriptionToStockWhenStockIsNotPresentInFinancialInstrumentsTable(){
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.STOCK;
    int upperThreshold = 10;
    int lowerThreshold = 10;

    when(subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(anyString(), any(), anyString())).thenReturn(false);
    when(financialInstrumentRepository.findBySymbolAndMarketType(cod, marketType)).thenReturn(Optional.empty());

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () ->
        marketHubService.subscribeUserToFinancialInstrument(email, cod, marketType, upperThreshold, lowerThreshold)
    );

    verify(financialInstrumentRepository).findBySymbolAndMarketType(cod, marketType);
    verify(subscriptionRepository).existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(cod, marketType, email);
    verify(subscriptionRepository, times(0)).save(any());
    verifyNoMoreInteractions(stockMarketAdapter);
    verifyNoInteractions(cryptoMarketAdapter);

    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  @Test
  void shouldAllowUserToUnsubscribeFromFinancialInstrumentNotificationsWhenUserHasAnOngoingSubscription(){
    String cod = "cod1";
    String email = "email@gmail.com";
    MarketType marketType = MarketType.STOCK;

    marketHubService.unSubscribeUserFromFinancialInstrumentNotifications(email, marketType, cod);

    verify(subscriptionRepository, times(1)).deleteByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(cod, marketType, email);
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
    verifyNoMoreInteractions(financialInstrumentRepository, cryptoMarketAdapter);
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

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(new ArrayList<>());
    var response = marketHubService.getSubscribedFinancialInstrumentsByUser(email).instruments();

    assertEquals(0, response.get(MarketType.STOCK).size());
    assertEquals(0, response.get(MarketType.CRYPTO).size());
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNotSubscriptions(){
    String email = "email@gmail.com";
    when(subscriptionRepository.findAllByEmail(email)).thenReturn(new ArrayList<>());

    var response = marketHubService.getSubscribedFinancialInstrumentsByUser(email).instruments();

    assertEquals(0, response.get(MarketType.STOCK).size());
    assertEquals(0, response.get(MarketType.CRYPTO).size());
  }

  @Test
  void shouldReturnOneElementListWhenUserHasOnlyOneCryptoCurrencySubscription(){
    String email = "email@gmail.com";
    FinancialInstrumentEntity expectedCryptoCurrency = FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency1.getSymbol())
        .marketType(MarketType.CRYPTO)
        .name(cryptoCurrency1.getName())
        .build();
    SubscriptionEntity expectedSubscription = SubscriptionEntity.builder()
        .financialInstrument(expectedCryptoCurrency)
        .email(email)
        .build();

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(List.of(expectedSubscription));
    when(cryptoMarketAdapter.fetchByIds(List.of(cryptoCurrency1.getName()))).thenReturn(Mono.just(List.of(cryptoCurrency1)));
    when(stockMarketAdapter.fetchByIds(anyList())).thenReturn(Mono.just(new ArrayList<>()));
    var response = marketHubService.getSubscribedFinancialInstrumentsByUser(email).instruments();

    assertEquals(0, response.get(MarketType.STOCK).size());
    assertEquals(1, response.get(MarketType.CRYPTO).size());
    assertEqualsListFinancialInstruments(List.of(cryptoCurrency1), response.get(MarketType.CRYPTO));
  }

  @Test
  void shouldReturnOneElementListWhenUserHasOnlyOneStockSubscription(){
    String email = "email@gmail.com";
    FinancialInstrumentEntity expectedStock = FinancialInstrumentEntity.builder()
        .symbol(stock1.getSymbol())
        .marketType(MarketType.STOCK)
        .name(stock1.getName())
        .build();
    SubscriptionEntity expectedSubscription = SubscriptionEntity.builder()
        .financialInstrument(expectedStock)
        .email(email)
        .build();

    when(subscriptionRepository.findAllByEmail(email)).thenReturn(List.of(expectedSubscription));
    when(stockMarketAdapter.fetchByIds(List.of(stock1.getSymbol()))).thenReturn(Mono.just(List.of(stock1)));
    when(cryptoMarketAdapter.fetchByIds(anyList())).thenReturn(Mono.just(new ArrayList<>()));
    var response = marketHubService.getSubscribedFinancialInstrumentsByUser(email).instruments();

    assertEquals(1, response.get(MarketType.STOCK).size());
    assertEquals(0, response.get(MarketType.CRYPTO).size());
    assertEqualsListFinancialInstruments(List.of(stock1), response.get(MarketType.STOCK));
  }

  @Test
  void shouldReturnBothCryptoAndStockListsWhenUserHasSubscriptionsToBoth() {
    String email = "email@gmail.com";

    FinancialInstrumentEntity expectedCryptoCurrency = FinancialInstrumentEntity.builder()
        .symbol(cryptoCurrency1.getSymbol())
        .marketType(MarketType.CRYPTO)
        .name(cryptoCurrency1.getName())
        .build();

    SubscriptionEntity cryptoSubscription = SubscriptionEntity.builder()
        .financialInstrument(expectedCryptoCurrency)
        .email(email)
        .build();

    FinancialInstrumentEntity expectedStock = FinancialInstrumentEntity.builder()
        .symbol(stock1.getSymbol())
        .marketType(MarketType.STOCK)
        .name(stock1.getName())
        .build();

    SubscriptionEntity stockSubscription = SubscriptionEntity.builder()
        .financialInstrument(expectedStock)
        .email(email)
        .build();

    when(subscriptionRepository.findAllByEmail(email))
        .thenReturn(List.of(cryptoSubscription, stockSubscription));

    when(cryptoMarketAdapter.fetchByIds(List.of(cryptoCurrency1.getName())))
        .thenReturn(Mono.just(List.of(cryptoCurrency1)));

    when(stockMarketAdapter.fetchByIds(List.of(stock1.getSymbol())))
        .thenReturn(Mono.just(List.of(stock1)));

    var response = marketHubService.getSubscribedFinancialInstrumentsByUser(email).instruments();

    assertEquals(1, response.get(MarketType.CRYPTO).size());
    assertEquals(1, response.get(MarketType.STOCK).size());
    assertEqualsListFinancialInstruments(List.of(cryptoCurrency1), response.get(MarketType.CRYPTO));
    assertEqualsListFinancialInstruments(List.of(stock1), response.get(MarketType.STOCK));
  }

  @Test
  void shouldNotNotifyAnyUserWhenThereAreNoSubscribers(){
    when(financialInstrumentRepository.findAllWithRelatedSubscriptions()).thenReturn(List.of());

    marketHubService.notifySubscribers();

    verify(financialInstrumentRepository).findAllWithRelatedSubscriptions();
    verifyNoMoreInteractions(subscriptionRepository, financialInstrumentRepository, cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldNotifySubscribersWhenExistsOneCryptoCurrencyWithAtLeastOneSubscription(){
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice() * 0.5);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());

    //TODO crear una abstraccion para la verificacion de las notificaciones
    verify(notificationService).notifyUsersByEmail(
        Map.of(
            subscription.getEmail(),
            String.format("Crypto symbol %s is now worth $%.2f", cryptoCurrency1.getSymbol(), cryptoCurrency1.getPrice())
        )
    );
    verifyNoMoreInteractions(cryptoMarketAdapter, stockMarketAdapter);
  }

  @Test
  void shouldNotifySubscribersWhenExistsOneStockWithAtLeastOneSubscription(){
    var financialInstrument = createFinancialInstrument(MarketType.STOCK, stock1);
    var subscription = createSubscription(financialInstrument, stock1.getPrice() * 0.5);

    mockNotifySubscribersDependencies(financialInstrument, subscription, new ArrayList<>(), List.of(stock1.getSymbol()), new ArrayList<>(), List.of(stock1));

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, new ArrayList<>(), List.of(financialInstrument.getSymbol()));
    verify(notificationService).notifyUsersByEmail(
        Map.of(
            subscription.getEmail(),
            String.format("Stock symbol %s is now worth $%.2f", stock1.getSymbol(), stock1.getPrice())
        )
    );
  }

  @Test
  void shouldNotNotifySubscribersWhenCryptoPriceIsAboveLowerThreshold() {
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice());

    cryptoCurrency1.setPrice(cryptoCurrency1.getPrice() * 0.91);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());
    verifyNoMoreInteractions(notificationService);
  }

  @Test
  void shouldNotNotifySubscribersWhenCryptoPriceIsBelowUpperThreshold() {
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice());

    cryptoCurrency1.setPrice(cryptoCurrency1.getPrice() * 1.09);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());
    verifyNoMoreInteractions(notificationService);
  }

  @Test
  void shouldNotifySubscribersWhenCryptoPriceIsBelowLowerThreshold() {
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice());

    cryptoCurrency1.setPrice(cryptoCurrency1.getPrice() * 0.89);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());
    verify(notificationService).notifyUsersByEmail(
        Map.of(
            subscription.getEmail(),
            String.format("Crypto symbol %s is now worth $%.2f", cryptoCurrency1.getSymbol(), cryptoCurrency1.getPrice())
        )
    );
  }

  @Test
  void shouldNotifySubscribersWhenCryptoPriceIsAboveUpperThreshold() {
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice());

    cryptoCurrency1.setPrice(cryptoCurrency1.getPrice() * 1.11);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());
    verify(notificationService).notifyUsersByEmail(
        Map.of(subscription.getEmail(), "Crypto symbol " + cryptoCurrency1.getSymbol() + " is now worth $" + cryptoCurrency1.getPrice())
    );
  }

  @Test
  void shouldUpdateSubscriptionReferencePriceWhenSubscriberReceivesNotification() {
    var financialInstrument = createFinancialInstrument(MarketType.CRYPTO, cryptoCurrency1);
    var subscription = createSubscription(financialInstrument, cryptoCurrency1.getPrice());

    cryptoCurrency1.setPrice(cryptoCurrency1.getPrice() * 1.11);

    mockNotifySubscribersDependencies(financialInstrument, subscription, List.of(cryptoCurrency1.getName()), new ArrayList<>(), List.of(cryptoCurrency1), new ArrayList<>());

    marketHubService.notifySubscribers();

    assertEquals(cryptoCurrency1.getPrice(), subscription.getLastReferencePrice());
    verify(subscriptionRepository).saveAll(List.of(subscription));
    verifyInteractions(financialInstrument, List.of(cryptoCurrency1.getName()), new ArrayList<>());
    verify(notificationService).notifyUsersByEmail(
        Map.of(subscription.getEmail(), "Crypto symbol " + cryptoCurrency1.getSymbol() + " is now worth $" + cryptoCurrency1.getPrice())
    );
  }

  private FinancialInstrumentEntity createFinancialInstrument(MarketType marketType, FinancialInstrument financialInstrument) {
    return FinancialInstrumentEntity.builder()
        .marketType(marketType)
        .name(financialInstrument.getName())
        .symbol(financialInstrument.getSymbol())
        .build();
  }

  private SubscriptionEntity createSubscription(FinancialInstrumentEntity financialInstrument, double lastPrice) {
    return SubscriptionEntity.builder()
        .financialInstrument(financialInstrument)
        .lastReferencePrice(lastPrice)
        .lowerThreshold(10)
        .upperThreshold(10)
        .email("email@gmail.com")
        .originalPrice(lastPrice)
        .build();
  }

  private void mockNotifySubscribersDependencies(FinancialInstrumentEntity financialInstrument, SubscriptionEntity subscription, List<String> cryptoAdapterParameter, List<String> stockAdapterParameter,List<CryptoCurrency> cryptoAdapterReturnValue, List<Stock> stockAdapterReturnValue) {
    when(financialInstrumentRepository.findAllWithRelatedSubscriptions()).thenReturn(List.of(financialInstrument));
    when(cryptoMarketAdapter.fetchByIds(cryptoAdapterParameter)).thenReturn(Mono.just(cryptoAdapterReturnValue));
    when(stockMarketAdapter.fetchByIds(stockAdapterParameter)).thenReturn(Mono.just(stockAdapterReturnValue));
    when(subscriptionRepository.findAllByFinancialInstrument(financialInstrument)).thenReturn(List.of(subscription));
    doNothing().when(notificationService).notifyUsersByEmail(Map.of(subscription.getEmail(), anyString()));
  }

  private void verifyInteractions(FinancialInstrumentEntity financialInstrument,
      List<String> cryptoAdapterParameter, List<String> stockAdapterParameter) {
    verify(financialInstrumentRepository).findAllWithRelatedSubscriptions();
    verify(cryptoMarketAdapter, times(1)).fetchByIds(cryptoAdapterParameter);
    verify(stockMarketAdapter, times(1)).fetchByIds(stockAdapterParameter);
    verify(subscriptionRepository, times(1)).findAllByFinancialInstrument(financialInstrument);
    verifyNoMoreInteractions(cryptoMarketAdapter, stockMarketAdapter);
  }

  private void assertEqualsListFinancialInstruments(List<? extends FinancialInstrument> expected, List<? extends FinancialInstrument> current){
    assertEquals(expected.size(), current.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).getSymbol(), current.get(i).getSymbol());
      assertEquals(expected.get(i).getName(), current.get(i).getName());
      assertEquals(expected.get(i).getPrice(), current.get(i).getPrice(), 0.01);
    }
  }
}