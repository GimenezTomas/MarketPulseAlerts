package com.tomas.market.pulse.alerts.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class FinancialInstrument {
  protected String symbol;
  protected String name;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  protected Double price;
}
