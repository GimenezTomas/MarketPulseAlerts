package com.tomas.market.pulse.alerts.api.services;

import java.util.ArrayList;
import java.util.List;

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
import com.tomas.market.pulse.alerts.model.SubscriptionEntity;
import com.tomas.market.pulse.alerts.repositories.SubscriptionEntityRepository;

import reactor.core.publisher.Mono;

@Service
public class MarketHubServiceImpl implements MarketHubService{
  private final MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter;
  private final MarketDataAdapter<Stock> stockMarketAdapter;
  private final SubscriptionEntityRepository subscriptionRepository;

  public MarketHubServiceImpl(MarketDataAdapter<CryptoCurrency> cryptoMarketAdapter,
      MarketDataAdapter<Stock> stockMarketAdapter, SubscriptionEntityRepository subscriptionRepository) {
    this.cryptoMarketAdapter = cryptoMarketAdapter;
    this.stockMarketAdapter = stockMarketAdapter;

    //TODO crear map de adapters

    this.subscriptionRepository = subscriptionRepository;
  }

  //TODO capaz seria mejor tener todos los instrumentos en la bd no era la idea pero para futuro
  @Override
  public FinancialInstrumentResponse getAll() {
    Mono<List<CryptoCurrency>> cryptoCurrenciesMono = cryptoMarketAdapter.fetchMarketData();
    Mono<List<Stock>> stockListMono = stockMarketAdapter.fetchMarketData();

    return Mono.zip(cryptoCurrenciesMono, stockListMono)
        .map(tuple -> new FinancialInstrumentResponse(tuple.getT1(), tuple.getT2())
        )
        .block();
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



    return new FinancialInstrumentResponse(cryptoCurrencies, stocks);
  }

  private MarketDataAdapter<? extends FinancialInstrument> getMarketAdapter(MarketType marketType) {
    return switch (marketType) {
      case CRYPTO -> cryptoMarketAdapter;
      case STOCK -> stockMarketAdapter;
    };
  }
}
