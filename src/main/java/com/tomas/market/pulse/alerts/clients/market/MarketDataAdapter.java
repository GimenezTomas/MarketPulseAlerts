package com.tomas.market.pulse.alerts.clients.market;

import java.util.List;

import com.tomas.market.pulse.alerts.model.FinancialInstrument;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;

import reactor.core.publisher.Mono;

public interface MarketDataAdapter<E extends FinancialInstrument> {
  Mono<List<E>> fetchMarketData();

  Mono<E> fetchByFinancialInstrument(FinancialInstrumentEntity financialInstrument);

  //TODO: rever tema de los IDS, debido a que depende del adapter que usar. A lo mejor lo propio seria pasarle un FinancialInstrument y ya (o un codigo xq resuelvo el probelma en la subscirpcion)
  Mono<List<E>> fetchByIds(List<String> ids);
}
