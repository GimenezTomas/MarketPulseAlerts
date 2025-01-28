package com.tomas.market.pulse.alerts.clients.market.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiClient;
import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoCryptoDTO;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;
import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CryptoMarketAdapterTest {
  @InjectMocks
  private CryptoMarketAdapter cryptoMarketAdapter;
  @Mock
  private CoinGeckoApiClient coinGeckoApiClient;

  @Test
  void shouldFetchMarketDataAndMapToCryptoCurrenciesList() {
    List<CryptoCurrency> expectedCryptos = List.of(new CryptoCurrency("symbol1", "name1", 0d, 0d, 0d), new CryptoCurrency("symbol2", "name2", 1d, 1d, 0d));

    when(coinGeckoApiClient.fetchMarketData(anyList())).thenReturn(
        Mono.just(List.of(new CoinGeckoCryptoDTO("id1", "symbol1", "name1", 0, 0, 0), new CoinGeckoCryptoDTO("id2", "symbol2", "name2", 1, 1, 0))));

    Mono<List<CryptoCurrency>> actualMono = cryptoMarketAdapter.fetchMarketData();

    assertEquals(expectedCryptos, actualMono.block());
  }


  @Test
  void shouldFetchByIdAndMapToCryptoCurrency(){
    var financialInstrumentEntity = FinancialInstrumentEntity.builder()
        .symbol("symbol1")
        .name("name1")
        .marketType(MarketType.CRYPTO)
        .build();
    CryptoCurrency expectedCrypto = new CryptoCurrency("symbol1", "name1", 0d, 0d, 0d);

    when(coinGeckoApiClient.fetchMarketData(anyList())).thenReturn(
        Mono.just(List.of(new CoinGeckoCryptoDTO("id1", "symbol1", "name1", 0d, 0d, 0d))));

    Mono<CryptoCurrency> actualMono = cryptoMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity);

    assertEquals(expectedCrypto, actualMono.block());
  }

  @Test
  void shouldThrowExceptionWhileFetchingByIdIfItDoesNotExist() {
    var financialInstrumentEntity = FinancialInstrumentEntity.builder()
        .symbol("symbol1")
        .name("name1")
        .marketType(MarketType.CRYPTO)
        .build();

    when(coinGeckoApiClient.fetchMarketData(List.of(financialInstrumentEntity.getName())))
        .thenReturn(Mono.just(List.of()));

    var monoResult = cryptoMarketAdapter.fetchByFinancialInstrument(financialInstrumentEntity);
    var e = assertThrows(ResponseStatusException.class, monoResult::block);
    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  @Test
  void shouldFetchByIdsAndMapToCryptoCurrenciesList(){
    CoinGeckoCryptoDTO coin1 = new CoinGeckoCryptoDTO("id1", "symbol1", "name1", 900, 0d, 0d);
    CoinGeckoCryptoDTO coin2 = new CoinGeckoCryptoDTO("id2", "symbol2", "name2", 700, 0d, 0d);

    when(coinGeckoApiClient.fetchMarketData(List.of(coin1.symbol(), coin2.symbol()))).thenReturn(Mono.just(List.of(coin1, coin2)));
    Mono<List<CryptoCurrency>> response = cryptoMarketAdapter.fetchByIds(List.of(coin1.symbol(), coin2.symbol()));
    var cryptoCurrencies = response.block();

    assertEquals(2, Objects.requireNonNull(cryptoCurrencies).size());

    assertEquals(coin1.symbol(), cryptoCurrencies.get(0).getSymbol());
    assertEquals(coin1.name(), cryptoCurrencies.get(0).getName());
    assertEquals(coin1.currentPrice(), cryptoCurrencies.get(0).getPrice());

    assertEquals(coin2.symbol(), cryptoCurrencies.get(1).getSymbol());
    assertEquals(coin2.name(), cryptoCurrencies.get(1).getName());
    assertEquals(coin2.currentPrice(), cryptoCurrencies.get(1).getPrice());
  }

}
