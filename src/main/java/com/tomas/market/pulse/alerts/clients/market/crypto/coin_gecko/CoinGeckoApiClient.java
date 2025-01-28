package com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class CoinGeckoApiClient {
  private final WebClient webClient;

  public CoinGeckoApiClient(@Qualifier("coinGeckoClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public Mono<List<CoinGeckoCryptoDTO>> fetchMarketData(List<String> ids) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/coins/markets")
            .queryParam("vs_currency", "usd")
            .queryParam("ids", String.join(",", ids.stream().map(String::toLowerCase).toList()))
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CoinGeckoCryptoDTO>>(){});
  }
}
