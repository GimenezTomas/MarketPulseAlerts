package com.tomas.market.pulse.alerts.clients.market;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiProperties;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.ProfitApiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MarketApiClientsConfig {
  private final CoinGeckoApiProperties coinGeckoApiProperties;
  private final ProfitApiProperties profitApiProperties;

  @Bean(name = "coinGeckoClient")
  public WebClient coinGeckoClient(WebClient.Builder builder){
    return builder
        .baseUrl(coinGeckoApiProperties.getBaseUrl())
        .defaultHeader("accept", "application/json")
        .defaultHeader("x-cg-demo-api-key", coinGeckoApiProperties.getApiKey())
        .filters(exchangeFilterFunctions -> {
          exchangeFilterFunctions.add(logRequest());
        })
        .build();
  }

  @Bean(name = "profitClient")
  public WebClient profitClient(WebClient.Builder builder) {
    String baseUrlWithToken = UriComponentsBuilder.fromUriString(profitApiProperties.getBaseUrl())
        .queryParam("token", profitApiProperties.getToken())
        .build()
        .toUriString();

    return builder
        .baseUrl(baseUrlWithToken)
        .defaultHeader("accept", "application/json")
        .filters(exchangeFilterFunctions -> {
          exchangeFilterFunctions.add(logRequest());
        })
        .build();
  }

  private static ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
      clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
      return Mono.just(clientRequest);
    });
  }
}