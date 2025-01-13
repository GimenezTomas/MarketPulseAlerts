package com.tomas.market.pulse.alerts.clients.market.stock.profit;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoCryptoDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
public class ProfitApiClient {
  private final WebClient profitApiWebClient;
  private final ObjectMapper objectMapper;

  public ProfitApiClient(@Qualifier("profitClient") WebClient profitApiWebClient, ObjectMapper objectMapper) {
    this.profitApiWebClient = profitApiWebClient;
    this.objectMapper = objectMapper;
  }

  public Mono<StockProfitDTO> fetchMarketData(String symbol) {
    return profitApiWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/market-data/quote/{symbol}")
            .build(symbol))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<StockProfitDTO>(){});
  }

  public Mono<List<StockProfitDTO>> fetchStocksData() {
    return profitApiWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/reference/stocks")
            .queryParam("limit", 20)
            .queryParam("country", "United States")
            .queryParam("available_data", "live")
            .queryParam("currency", "USD")
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .map(response -> {
          Object data = response.get("data");
            return ((List<?>) data).stream()
                .map(item -> objectMapper.convertValue(item, StockProfitDTO.class))
                .toList();
        });
  }
}
