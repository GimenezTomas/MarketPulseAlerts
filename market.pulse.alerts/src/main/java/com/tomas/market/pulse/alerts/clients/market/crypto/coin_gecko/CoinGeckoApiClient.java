package com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Setter;
import reactor.core.publisher.Mono;

@Service
public class CoinGeckoApiClient {
  private final WebClient coinGeckoApiClient;

  public CoinGeckoApiClient(@Qualifier("coinGeckoClient") WebClient coinGeckoApiClient) {
    this.coinGeckoApiClient = coinGeckoApiClient;
  }

  public Mono<List<CoinGeckoCryptoDTO>> fetchMarketData(List<String> ids) {
    return coinGeckoApiClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/coins/markets")
            .queryParam("vs_currency", "usd")
            .queryParam("ids", String.join(",", ids))
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CoinGeckoCryptoDTO>>(){});
  }
}
