package com.tomas.market.pulse.alerts.clients.market.crypto;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tomas.market.pulse.alerts.clients.market.MarketDataAdapter;
import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiClient;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class CryptoMarketAdapter implements MarketDataAdapter<CryptoCurrency> {
  private final CoinGeckoApiClient coinGeckoApiClient;
  private static final List<String> IDS = List.of("bitcoin", "ethereum");

  @Override
  public Mono<List<CryptoCurrency>> fetchMarketData() {
    return coinGeckoApiClient.fetchMarketData(IDS)
        .map(dtoList -> dtoList.stream()
            .map(dto -> new CryptoCurrency(dto.symbol(), dto.name(), dto.currentPrice()))
            .toList());
  }

  @Override
  public Mono<CryptoCurrency> fetchById(String id) {
    return null;
  }
}
