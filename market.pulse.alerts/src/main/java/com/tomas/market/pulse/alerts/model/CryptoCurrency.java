package com.tomas.market.pulse.alerts.model;

//TODO por ahora sobremodele, no hay ningun motivo por lo que no pueda usar FinancialInstrument
public class CryptoCurrency extends FinancialInstrument{
  public CryptoCurrency(String symbol, String name, double price) {
    super(symbol, name, price);
  }
}
