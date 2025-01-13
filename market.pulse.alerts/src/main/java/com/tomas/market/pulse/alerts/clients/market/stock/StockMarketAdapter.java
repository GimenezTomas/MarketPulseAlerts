package com.tomas.market.pulse.alerts.clients.market.stock;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tomas.market.pulse.alerts.clients.market.MarketDataAdapter;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.ProfitApiClient;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.StockProfitDTO;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.Stock;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class StockMarketAdapter implements MarketDataAdapter<Stock> {
  private final ProfitApiClient profitApiClient;

  @Override
  public Mono<List<Stock>> fetchMarketData() {
    return profitApiClient.fetchStocksData()
        .map(dtoList -> dtoList.stream()
            .map(dto -> new Stock(dto.symbol(), dto.name(), dto.price()))
            .toList());
  }

  @Override
  public Mono<Stock> fetchById(String id) {
    return null;
  }

  //TODO hay que encontrarle la vuelta para que todas las llamadas de cada uno de sus quotes (tesla x ej) los haga de forma asincrona
}
