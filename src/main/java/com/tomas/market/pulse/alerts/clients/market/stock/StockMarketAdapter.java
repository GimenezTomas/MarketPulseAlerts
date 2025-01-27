package com.tomas.market.pulse.alerts.clients.market.stock;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.MarketDataAdapter;
import com.tomas.market.pulse.alerts.clients.market.stock.profit.ProfitApiClient;
import com.tomas.market.pulse.alerts.model.Stock;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
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
  public Mono<Stock> fetchByFinancialInstrument(FinancialInstrumentEntity financialInstrument) {
    return fetchBySymbol(financialInstrument.getSymbol());
  }

  @Override
  public Mono<List<Stock>> fetchByIds(List<String> ids) {
    return Flux.fromIterable(ids)
        .flatMap(id -> fetchBySymbol(id).onErrorResume(e -> Mono.empty()))
        .collectList();
  }

  private Mono<Stock> fetchBySymbol(String symbol){
    return profitApiClient.fetchStockById(symbol)
        .flatMap(stock -> stock.symbol() == null
            ? Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"))
            : Mono.just(new Stock(stock.symbol(), stock.name(), stock.price())));
  }
}
