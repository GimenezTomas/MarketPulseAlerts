package com.tomas.market.pulse.alerts.model.entities;


import com.tomas.market.pulse.alerts.model.MarketType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "financial_instruments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialInstrumentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String symbol;

  @Column
  private String name;

  @Column(name = "market_type")
  @Enumerated(EnumType.STRING)
  private MarketType marketType;
}
