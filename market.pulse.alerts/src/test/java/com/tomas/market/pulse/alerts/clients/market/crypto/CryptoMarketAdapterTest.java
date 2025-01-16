package com.tomas.market.pulse.alerts.clients.market.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiClient;
import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoCryptoDTO;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;

import reactor.core.publisher.Mono;
@MockitoSettings(strictness = Strictness.LENIENT)
class CryptoMarketAdapterTest {
  @InjectMocks
  private CryptoMarketAdapter cryptoMarketAdapter;
  @Mock
  private CoinGeckoApiClient coinGeckoApiClient;

  @Test
  void shouldFetchMarketDataAndMapToStockList() {
    List<CryptoCurrency> expectedCryptos = List.of(new CryptoCurrency("symbol1", "name1", 0), new CryptoCurrency("symbol2", "name2", 1));

    when(coinGeckoApiClient.fetchMarketData(anyList())).thenReturn(
        Mono.just(List.of(new CoinGeckoCryptoDTO("id1", "symbol1", "name1", 0), new CoinGeckoCryptoDTO("id2", "symbol2", "name2", 1))));

    Mono<List<CryptoCurrency>> actualMono = cryptoMarketAdapter.fetchMarketData();

    assertEquals(expectedCryptos, actualMono.block());
  }


  @Test
  void shouldFetchByIdAndMapToStock(){
    CryptoCurrency expectedCrypto = new CryptoCurrency("symbol1", "name1", 0);

    when(coinGeckoApiClient.fetchMarketData(anyList())).thenReturn(
        Mono.just(List.of(new CoinGeckoCryptoDTO("id1", "symbol1", "name1", 0))));

    Mono<CryptoCurrency> actualMono = cryptoMarketAdapter.fetchById("symbol1");

    assertEquals(expectedCrypto, actualMono.block());
  }

  @Test
  void shouldThrowExceptionWhileFetchingByIdIfItDoesNotExist() {
    String symbol = "symbol1";

    when(coinGeckoApiClient.fetchMarketData(List.of(symbol)))
        .thenReturn(Mono.just(List.of()));

    var e = assertThrows(ResponseStatusException.class, () -> cryptoMarketAdapter.fetchById(symbol).block());
    assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
  }

}
