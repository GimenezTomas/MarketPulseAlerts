package com.tomas.market.pulse.alerts.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CryptoCurrency extends FinancialInstrument{
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double maxPrice24h;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double minPrice24h;

  public CryptoCurrency(String symbol, String name, Double price, Double maxPrice24h, Double minPrice24h) {
    super(symbol, name, price);
    this.maxPrice24h = maxPrice24h;
    this.minPrice24h = minPrice24h;
  }

  public CryptoCurrency(String symbol, String name, double price) {
    super(symbol, name, price);
  }
}
