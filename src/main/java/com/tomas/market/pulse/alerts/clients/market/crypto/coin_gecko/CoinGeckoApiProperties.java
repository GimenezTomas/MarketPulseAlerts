package com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "external.rest-apis.coin-gecko")
public class CoinGeckoApiProperties {
  private String baseUrl;
  private String apiKey;
}
