package com.tomas.market.pulse.alerts.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class FinancialInstrument {
  protected String symbol;
  protected String name;
  //TODO sacar price
  protected double price;
}
