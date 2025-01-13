package com.tomas.market.pulse.alerts.api.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tomas.market.pulse.alerts.api.dtos.FinancialInstrumentResponse;
import com.tomas.market.pulse.alerts.clients.market.stock.StockMarketAdapter;
import com.tomas.market.pulse.alerts.clients.market.crypto.CryptoMarketAdapter;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.Stock;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class MarketHubServiceImpl implements MarketHubService{
  private final CryptoMarketAdapter cryptoMarketAdapter;
  private final StockMarketAdapter stockMarketAdapter;

  //TODO hacer que esto devuelva un response con cada lista por separado
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
  public void subscribeUserToFinancialInstrument(String email, String financialInstrumentId) {

  }

}
