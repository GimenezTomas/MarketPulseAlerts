package com.tomas.market.pulse.alerts.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;

import com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko.CoinGeckoApiClient;
import com.tomas.market.pulse.alerts.model.CryptoCurrency;

public class CoinGeckoApiClientTest {
  @InjectMocks
  private CoinGeckoApiClient coinGeckoApiClient;
  @Mock
  private WebClient webClient;

  @BeforeEach
  void setUp(){
  }

  @Test
  void shouldReturnListOfCurrenciesWithOneElementWhenIdsListHasOneId(){
//    coinGeckoApiClient.setIds(List.of("bitcoin"));

    //List<CryptoCurrency> cryptoCurrencies = coinGeckoApiClient.fetchMarketData();
    //assertEquals(1, cryptoCurrencies.size());
  }
}
