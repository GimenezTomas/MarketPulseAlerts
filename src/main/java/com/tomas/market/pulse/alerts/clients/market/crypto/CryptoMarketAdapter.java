package com.tomas.market.pulse.alerts.clients.market.crypto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.MarketDataAdapter;
import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiClient;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class CryptoMarketAdapter implements MarketDataAdapter<CryptoCurrency> {
  private final CoinGeckoApiClient coinGeckoApiClient;
  private static final List<String> IDS = List.of("bitcoin", "ethereum");

  @Override
  public Mono<List<CryptoCurrency>> fetchMarketData() {
    return this.fetchByIds(IDS);
  }

  @Override
  public Mono<CryptoCurrency> fetchByFinancialInstrument(FinancialInstrumentEntity financialInstrument) {
    return coinGeckoApiClient.fetchMarketData(List.of(financialInstrument.getName()))
        .flatMap(dtoList -> dtoList.isEmpty()
            ? Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "CryptoCurrency not found"))
            : Mono.just(dtoList.get(0)))
        .map(dto -> new CryptoCurrency(dto.symbol(), dto.name(), dto.currentPrice(), dto.high24h(), dto.low24h()));
  }

  @Override
  public Mono<List<CryptoCurrency>> fetchByIds(List<String> ids) {
    return coinGeckoApiClient.fetchMarketData(ids)
        .map(dtoList -> dtoList.stream()
            .map(dto -> new CryptoCurrency(dto.symbol(), dto.name(), dto.currentPrice(), dto.high24h(), dto.low24h()))
            .toList());
  }
}
