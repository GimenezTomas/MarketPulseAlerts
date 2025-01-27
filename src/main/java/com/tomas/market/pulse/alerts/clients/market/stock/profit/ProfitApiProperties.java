package com.tomas.market.pulse.alerts.clients.market.stock.profit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "external.rest-apis.profit")
public class ProfitApiProperties {
  private String baseUrl;
  private String token;
}