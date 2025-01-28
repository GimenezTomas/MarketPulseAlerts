package com.tomas.market.pulse.alerts.api.services;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

import ch.qos.logback.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class MarketHubServiceImpl implements MarketHubService{
  private final MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter;
  private final MarketDataAdapter<Stock> stockMarketAdapter;
  private final SubscriptionEntityRepository subscriptionRepository;
  private final FinancialInstrumentEntityRepository financialInstrumentRepository;
  private final NotificationService notificationService;
  private final Map<MarketType, MarketDataAdapter> adapterMap;

  public MarketHubServiceImpl(MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter,
      MarketDataAdapter<Stock> stockMarketAdapter, SubscriptionEntityRepository subscriptionRepository,
      FinancialInstrumentEntityRepository financialInstrumentRepository, NotificationService notificationService) {
    this.cryptoMarketAdapter = cryptoMarketAdapter;
    this.stockMarketAdapter = stockMarketAdapter;

    //TODO crear map de adapters
    adapterMap = new EnumMap<>(MarketType.class);
    adapterMap.put(MarketType.CRYPTO, cryptoMarketAdapter);

    this.subscriptionRepository = subscriptionRepository;
    this.financialInstrumentRepository = financialInstrumentRepository;
    this.notificationService = notificationService;
  }

  @Override
  public FinancialInstrumentResponse getAll() {
    List<FinancialInstrumentEntity> instrumentEntities = financialInstrumentRepository.findAll();

    Map<MarketType, List<? extends FinancialInstrument>> instrumentsMap = new EnumMap<>(MarketType.class);
    instrumentsMap.put(MarketType.CRYPTO, instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.CRYPTO))
        .map(i -> new CryptoCurrency(i.getSymbol(), i.getName(), 0))
        .toList());

    instrumentsMap.put(MarketType.STOCK, instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.STOCK))
        .map(i -> new Stock(i.getSymbol(), i.getName(), 0))
        .toList());

    return new FinancialInstrumentResponse(instrumentsMap);
  }

  @Override
  public void subscribeUserToFinancialInstrument(String email, String symbol, MarketType marketType,
      int upperThreshold, int lowerThreshold) {
    if (subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(symbol, marketType, email))
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s is already subscribed to %s", email, symbol));

    var adapter = getMarketAdapter(marketType);
    var financialInstrument = financialInstrumentRepository.findBySymbolAndMarketType(symbol, marketType).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial instrument not found"));
    var result = adapter.fetchByFinancialInstrument(financialInstrument).block();

    SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
        .email(email)
        .financialInstrument(financialInstrument)
        .lastReferencePrice(Objects.requireNonNull(result).getPrice())
        .originalPrice(result.getPrice())
        .lowerThreshold(lowerThreshold)
        .upperThreshold(upperThreshold)
        .build();
    subscriptionRepository.save(subscriptionEntity);
  }

  @Transactional
  @Override
  public void unSubscribeUserFromFinancialInstrumentNotifications(String email, MarketType marketType, String financialInstrumentId) {
    subscriptionRepository.deleteByFinancialInstrument_SymbolAndFinancialInstrument_MarketTypeAndEmail(financialInstrumentId, marketType ,email);
  }

  @Override
  public FinancialInstrumentResponse getSubscribedFinancialInstrumentsByUser(String email) {
    var subscriptions = subscriptionRepository.findAllByEmail(email);
    List<FinancialInstrumentEntity> instrumentEntities = subscriptions.stream()
        .map(SubscriptionEntity::getFinancialInstrument)
        .toList();

    var cryptoNames = filterByMarketTypeAndMapToIdsList(MarketType.CRYPTO,
        FinancialInstrumentEntity::getName, instrumentEntities);

    var stockSymbols = filterByMarketTypeAndMapToIdsList(MarketType.STOCK,
        FinancialInstrumentEntity::getSymbol, instrumentEntities);

    BiFunction<List<CryptoCurrency>, List<Stock>, FinancialInstrumentResponse> mapFunction = (cryptoCurrencies, stocks) -> {
      Map<MarketType, List<? extends FinancialInstrument>> instrumentsMap = new EnumMap<>(MarketType.class);
      instrumentsMap.put(MarketType.CRYPTO, cryptoCurrencies);
      instrumentsMap.put(MarketType.STOCK, stocks);

      return new FinancialInstrumentResponse(instrumentsMap);
    };
    return fetchFinancialInstrumentsById(cryptoNames, stockSymbols, mapFunction)
        .block();
  }

  @Override
  public void syncNewFinancialInstruments() {
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = cryptoMarketAdapter.fetchMarketData();
    Mono<List<Stock>> stockListMono = stockMarketAdapter.fetchMarketData();

    Mono.zip(cryptoCurrenciesMono, stockListMono)
        .publishOn(Schedulers.boundedElastic())
        .map(tuple -> {
          List<String> symbols = new ArrayList<>(tuple.getT1().stream().map(CryptoCurrency::getSymbol).toList());
          symbols.addAll(tuple.getT2().stream().map(Stock::getSymbol).toList());

          var financialInstruments = financialInstrumentRepository.findAllBySymbolIn(symbols);

          Set<String> symbolsInRepo = financialInstruments.stream()
              .map(FinancialInstrumentEntity::getSymbol)
              .collect(Collectors.toSet());

          List<FinancialInstrumentEntity> missingFinancialInstruments = new ArrayList<>();
          missingFinancialInstruments.addAll(filterAndMap(tuple.getT1(), symbolsInRepo, MarketType.CRYPTO));
          missingFinancialInstruments.addAll(filterAndMap(tuple.getT2(), symbolsInRepo, MarketType.STOCK));

          return financialInstrumentRepository.saveAll(missingFinancialInstruments);
        }).block();
  }

  @Override
  public void notifySubscribers() {
    var financialInstruments = financialInstrumentRepository.findAllWithRelatedSubscriptions();

    if (financialInstruments.isEmpty()) {
      log.info("No notifications were sent because there were no subscriptions");
      return;
    }

    List<FinancialInstrumentEntity> cryptoList = financialInstruments.stream()
        .filter(f -> f.getMarketType() == MarketType.CRYPTO)
        .toList();

    List<FinancialInstrumentEntity> stocksList = financialInstruments.stream()
        .filter(f -> f.getMarketType() == MarketType.STOCK)
        .toList();

    Map<String, Double> cryptoSymbolsWithPrices = new HashMap<>();
    Map<String, Double> stockSymbolsWithPrices = new HashMap<>();

    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = cryptoMarketAdapter.fetchByIds(cryptoList.stream().map(FinancialInstrumentEntity::getName).toList());
    Mono<List<Stock>> stocksMono = stockMarketAdapter.fetchByIds(stocksList.stream().map(FinancialInstrumentEntity::getSymbol).toList());
    Mono.zip(cryptoCurrenciesMono, stocksMono)
        .doOnNext(tuple -> {
          tuple.getT1().forEach(c -> cryptoSymbolsWithPrices.put(c.getSymbol(), c.getPrice()));
          tuple.getT2().forEach(s -> stockSymbolsWithPrices.put(s.getSymbol(), s.getPrice()));
        })
        .block();

    processSubscribersToBeNotified(cryptoList, cryptoSymbolsWithPrices);
    processSubscribersToBeNotified(stocksList, stockSymbolsWithPrices);
  }

  //Supuestamente esta es la forma de hacerlo asincrono, no se bien como testearlo pero habria que haberlo testeado antes de hacerlo TDD
  //TODO testear c/6790654b-f7f0-8003-bd39-24a237feed75
  private void processSubscribersToBeNotified(
      List<FinancialInstrumentEntity> instruments,
      Map<String, Double> prices
  ) {
    Flux.fromIterable(instruments)
        .flatMap(instrument -> {
          double instrumentPrice =  prices.get(instrument.getSymbol());

          var subscriptions = subscriptionRepository.findAllByFinancialInstrument(instrument).stream()
              .filter(subscriptionEntity -> shouldNotify(subscriptionEntity, instrumentPrice))
              .toList();

          subscriptions.forEach(subscriptionEntity -> subscriptionEntity.setLastReferencePrice(instrumentPrice));
          subscriptionRepository.saveAll(subscriptions);

          var emailsAndMessages = subscriptions.stream()
              .collect(Collectors.toMap(
                  SubscriptionEntity::getEmail,
                  subscription -> buildNotificationMessage(instrument, instrumentPrice)
              ));

          if (!emailsAndMessages.isEmpty())
            return Mono.fromRunnable(() -> notificationService.notifyUsersByEmail(emailsAndMessages));

          return Mono.empty();
        })
        .subscribe();
  }

  private List<String> filterByMarketTypeAndMapToIdsList(MarketType marketType, Function<FinancialInstrumentEntity, String> mapFunction, List<FinancialInstrumentEntity> instrumentEntities){
    return instrumentEntities.stream()
        .filter(i -> i.getMarketType() == marketType)
        .map(mapFunction)
        .toList();
  }

  private <R> Mono<R> fetchFinancialInstrumentsById(List<String> cryptoCurrenciesNames, List<String> stockSymbols, BiFunction<List<CryptoCurrency>, List<Stock>, R> mapFunction) {
    Mono<List<CryptoCurrency>> cryptoCurrencies = !cryptoCurrenciesNames.isEmpty() ? cryptoMarketAdapter.fetchByIds(cryptoCurrenciesNames) : Mono.just(new ArrayList<>());
    Mono<List<Stock>> stocks = !stockSymbols.isEmpty() ? stockMarketAdapter.fetchByIds(stockSymbols) : Mono.just(new ArrayList<>());

    return Mono.zip(cryptoCurrencies, stocks, mapFunction);
  }

  private boolean shouldNotify(SubscriptionEntity subscription, double currentPrice) {
    double subsctiptionLastPrice = subscription.getLastReferencePrice();
    double upperThresholdPrice = calculateUpperThreshold(subsctiptionLastPrice, subscription.getUpperThreshold());
    double lowerThresholdPrice = calculateLowerThreshold(subsctiptionLastPrice, subscription.getLowerThreshold());

    return currentPrice >= upperThresholdPrice || currentPrice <= lowerThresholdPrice;
  }

  private double calculateUpperThreshold(double price, int percentage) {
    return price + calculatePercentage(price, percentage);
  }

  private double calculateLowerThreshold(double price, int percentage) {
    return price - calculatePercentage(price, percentage);
  }

  private double calculatePercentage(double base, int percentage) {
    return base * percentage / 100.0;
  }

  private String buildNotificationMessage(FinancialInstrumentEntity crypto, double price) {
    var marketTypeCapitalized = StringUtil.capitalizeFirstLetter(crypto.getMarketType().toString().toLowerCase());
    return String.format("%s symbol %s is now worth $%.2f", marketTypeCapitalized,crypto.getSymbol(), price);
  }

  private MarketDataAdapter<? extends FinancialInstrument> getMarketAdapter(MarketType marketType) {
    return switch (marketType) {
      case CRYPTO -> cryptoMarketAdapter;
      case STOCK -> stockMarketAdapter;
    };
  }

  private <T extends FinancialInstrument> List<FinancialInstrumentEntity> filterAndMap(List<T> instruments, Set<String> symbolsInRepo, MarketType marketType) {
    return instruments.stream()
        .filter(i -> !symbolsInRepo.contains(i.getSymbol()))
        .map(i -> FinancialInstrumentEntity.builder()
            .marketType(marketType)
            .symbol(i.getSymbol())
            .name(i.getName())
            .build())
        .toList();
  }
}
