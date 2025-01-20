package com.tomas.market.pulse.alerts.api.services;

import java.util.ArrayList;
import java.util.List;
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

import reactor.core.publisher.Mono;

@Service
public class MarketHubServiceImpl implements MarketHubService{
  private final MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter;
  private final MarketDataAdapter<Stock> stockMarketAdapter;
  private final SubscriptionEntityRepository subscriptionRepository;
  private final FinancialInstrumentEntityRepository financialInstrumentRepository;

  public MarketHubServiceImpl(MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter,
      MarketDataAdapter<Stock> stockMarketAdapter, SubscriptionEntityRepository subscriptionRepository,
      FinancialInstrumentEntityRepository financialInstrumentRepository) {
    this.cryptoMarketAdapter = cryptoMarketAdapter;
    this.stockMarketAdapter = stockMarketAdapter;

    //TODO crear map de adapters

    this.subscriptionRepository = subscriptionRepository;
    this.financialInstrumentRepository = financialInstrumentRepository;
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

  @Override
  public void subscribeUserToFinancialInstrument(String email, String financialInstrumentId, MarketType marketType,
      int upperThreshold, int lowerThreshold) {
    if (subscriptionRepository.existsSubscriptionEntityByFinancialInstrumentIdAndEmail(financialInstrumentId, email))
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s is already subscribed to %s", email, financialInstrumentId));

    var adapter = getMarketAdapter(marketType);
    var result = adapter.fetchById(financialInstrumentId).block();

    SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
        .email(email)
        .financialInstrumentId(financialInstrumentId)
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
    subscriptionRepository.deleteByFinancialInstrumentIdAndEmail(financialInstrumentId, email);
  }

  @Override
  public FinancialInstrumentResponse getSubscribedFinancialInstrumentsByUserAndMarketTypes(String email,
      List<MarketType> markets) {
    List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    List<Stock> stocks = new ArrayList<>();

    var subscriptions = subscriptionRepository.findAllByEmail(email);
    List<String> intrumentsIds = subscriptions.stream().map(SubscriptionEntity::getFinancialInstrumentId).toList();


    return new FinancialInstrumentResponse(cryptoCurrencies, stocks);
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

  private MarketDataAdapter<? extends FinancialInstrument> getMarketAdapter(MarketType marketType) {
    return switch (marketType) {
      case CRYPTO -> cryptoMarketAdapter;
      case STOCK -> stockMarketAdapter;
    };
  }
}
