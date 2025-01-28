package com.tomas.market.pulse.alerts.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Stock extends FinancialInstrument{
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String ticker;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String broker;

  public Stock(String symbol, String name, double price) {
    super(symbol, name, price);
  }

  public Stock(String symbol, String name, double price, String ticker, String broker) {
    super(symbol, name, price);

    this.ticker = ticker;
    this.broker = broker;
  }
}
