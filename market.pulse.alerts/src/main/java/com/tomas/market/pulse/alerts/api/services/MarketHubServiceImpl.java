package com.tomas.market.pulse.alerts.api.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MarketHubServiceImpl implements MarketHubService{
  private final MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter;
  private final MarketDataAdapter<Stock> stockMarketAdapter;
  private final SubscriptionEntityRepository subscriptionRepository;
  private final FinancialInstrumentEntityRepository financialInstrumentRepository;
  private final NotificationService notificationService;

  public MarketHubServiceImpl(MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter,
      MarketDataAdapter<Stock> stockMarketAdapter, SubscriptionEntityRepository subscriptionRepository,
      FinancialInstrumentEntityRepository financialInstrumentRepository, NotificationService notificationService) {
    this.cryptoMarketAdapter = cryptoMarketAdapter;
    this.stockMarketAdapter = stockMarketAdapter;

    //TODO crear map de adapters

    this.subscriptionRepository = subscriptionRepository;
    this.financialInstrumentRepository = financialInstrumentRepository;
    this.notificationService = notificationService;
  }

  @Override
  public FinancialInstrumentResponse getAll() {
    List<FinancialInstrumentEntity> instrumentEntities = financialInstrumentRepository.findAll();

    List<CryptoCurrency> cryptoCurrencies = instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.CRYPTO))
        .map(i -> new CryptoCurrency(i.getSymbol(), i.getName(), 0))
        .toList();

    List<Stock> stocks = instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.STOCK))
        .map(i -> new Stock(i.getSymbol(), i.getName(), 0))
        .toList();

    return new FinancialInstrumentResponse(cryptoCurrencies, stocks);
  }

  //el flujo creo deberia ser entra simbolo y tipo de mercado -> financialIntrumentEntity -> funcion que sabe como

  @Override
  public void subscribeUserToFinancialInstrument(String email, String symbol, MarketType marketType,
      int upperThreshold, int lowerThreshold) {
    if (subscriptionRepository.existsSubscriptionEntityByFinancialInstrument_SymbolAndEmail(symbol, email))
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s is already subscribed to %s", email, symbol));

    var adapter = getMarketAdapter(marketType);
    var financialInstrument = financialInstrumentRepository.findBySymbolAndMarketType(symbol, marketType).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial instrument not found"));
    var result = adapter.fetchByFinancialInstrument(financialInstrument).block();

    SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
        .email(email)
        .financialInstrument(financialInstrument)
        .currentPrice(result.getPrice())
        .originalPrice(result.getPrice())
        .lowerThreshold(lowerThreshold)
        .upperThreshold(upperThreshold)
        .build();
    subscriptionRepository.save(subscriptionEntity);
  }

  @Transactional
  @Override
  public void unSubscribeUserFromFinancialInstrumentNotifications(String email, String financialInstrumentId) {
    subscriptionRepository.deleteByFinancialInstrument_SymbolAndEmail(financialInstrumentId, email);
  }

  @Override
  public FinancialInstrumentResponse getSubscribedFinancialInstrumentsByUser(String email) {
    var subscriptions = subscriptionRepository.findAllByEmail(email);
    List<FinancialInstrumentEntity> instrumentEntities = subscriptions.stream()
        .map(SubscriptionEntity::getFinancialInstrument)
        .toList();

    //TODO: rever tema de los IDS, debido a que depende del adapter que usar. A lo mejor lo propio seria pasarle un FinancialInstrument y ya (o un codigo xq resuelvo el probelma en la subscirpcion)
    var cryptoSymbols = instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.CRYPTO))
        .map(FinancialInstrumentEntity::getName)
        .toList();

    var stockNames = instrumentEntities.stream()
        .filter(i -> i.getMarketType().equals(MarketType.STOCK))
        .map(FinancialInstrumentEntity::getSymbol)
        .toList();

    Mono<List<CryptoCurrency>> cryptoCurrencies = !cryptoSymbols.isEmpty() ? cryptoMarketAdapter.fetchByIds(cryptoSymbols) : Mono.just(new ArrayList<>());
    Mono<List<Stock>> stocks = !stockNames.isEmpty() ? stockMarketAdapter.fetchByIds(stockNames) : Mono.just(new ArrayList<>());

    return Mono.zip(cryptoCurrencies, stocks)
        .map(tuple -> new FinancialInstrumentResponse(tuple.getT1(), tuple.getT2()))
        .block();
  }

  @Override
  public void syncNewFinancialInstruments() {
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = cryptoMarketAdapter.fetchMarketData();
    Mono<List<Stock>> stockListMono = stockMarketAdapter.fetchMarketData();

    Mono.zip(cryptoCurrenciesMono, stockListMono)
        .map(tuple -> {
          List<String> symbols = new ArrayList<>(tuple.getT1().stream().map(CryptoCurrency::getSymbol).toList());
          symbols.addAll(tuple.getT2().stream().map(Stock::getSymbol).toList());

          var financialInstruments = financialInstrumentRepository.findAllBySymbolIn(symbols);

          Set<String> symbolsInRepo = financialInstruments.stream()
              .map(FinancialInstrumentEntity::getSymbol)
              .collect(Collectors.toSet());

          List<FinancialInstrumentEntity> missingFinancialInstruments = new ArrayList<>(tuple.getT1().stream()
              .filter(crypto -> !symbolsInRepo.contains(crypto.getSymbol())) //TODO habria que corregir agregando la vallidacion extra del tipo de mercado para que ademas de existir el simbolo pertenezca a ese mercado
              .map(c -> FinancialInstrumentEntity.builder()
                  .name(c.getName())
                  .symbol(c.getSymbol())
                  .marketType(MarketType.CRYPTO)
                  .build()
              )
              .toList());

          missingFinancialInstruments.addAll(tuple.getT2().stream()
              .filter(stock -> !symbolsInRepo.contains(stock.getSymbol()))
              .map(s -> FinancialInstrumentEntity.builder()
                  .name(s.getName())
                  .symbol(s.getSymbol())
                  .marketType(MarketType.STOCK)
                  .build())
              .toList()
          );

          return financialInstrumentRepository.saveAll(missingFinancialInstruments);
        }).block();
  }

  @Override
  public void notifySubscribers() {
    var financialInstruments = financialInstrumentRepository.findAllWithRelatedSubscriptions();

    if (financialInstruments.isEmpty()) {
      log.info("no user was notified due to there are not subscriptions");
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

    //TODO esto deberia ser async
    cryptoList.forEach(c -> {
      var subscriptions = subscriptionRepository.findAllByFinancialInstrument(c);
      //TODO cambiarlo para que si es un low mande un msj en particular y si es un upper tmb y  fijarse si lo del diccionario se puede hacer directo con el .map
      Map<String, String> emailsAndMessages = new HashMap<>();
      subscriptions.stream()
          .filter(s -> (s.getCurrentPrice() * s.getUpperThreshold() / 100) + s.getCurrentPrice() <= cryptoSymbolsWithPrices.get(c.getSymbol()) || s.getCurrentPrice() - (s.getCurrentPrice() * s.getLowerThreshold() / 100) >= cryptoSymbolsWithPrices.get(c.getSymbol()))
          .forEach(s -> emailsAndMessages.put(s.getEmail(), String.format("Crypto symbol %s is now worth $%s", c.getSymbol(), cryptoSymbolsWithPrices.get(c.getSymbol()))));
      notificationService.notifyUsersByEmail(emailsAndMessages);
    });
    stocksList.forEach(st -> {
      var subscriptions = subscriptionRepository.findAllByFinancialInstrument(st);

      Map<String, String> emailsAndMessages = new HashMap<>();
      subscriptions.stream()
          .filter(s -> (s.getCurrentPrice() * s.getUpperThreshold() / 100) + s.getCurrentPrice() <= stockSymbolsWithPrices.get(st.getSymbol()) || s.getCurrentPrice() - (s.getCurrentPrice() * s.getLowerThreshold() / 100) >= stockSymbolsWithPrices.get(st.getSymbol()))
          .forEach(s -> emailsAndMessages.put(s.getEmail(), String.format("Stock symbol %s is now worth $%s", st.getSymbol(), stockSymbolsWithPrices.get(st.getSymbol()))));
      notificationService.notifyUsersByEmail(emailsAndMessages);
    });
  }

  private MarketDataAdapter<? extends FinancialInstrument> getMarketAdapter(MarketType marketType) {
    return switch (marketType) {
      case CRYPTO -> cryptoMarketAdapter;
      case STOCK -> stockMarketAdapter;
    };
  }
}
