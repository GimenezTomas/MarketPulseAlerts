package com.tomas.market.pulse.alerts.api.dtos;

import java.util.List;

import com.tomas.market.pulse.alerts.model.FinancialInstrument;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialInstrumentResponse{
  private List<? extends FinancialInstrument> crypto;
  private List<? extends FinancialInstrument> stock;
}
