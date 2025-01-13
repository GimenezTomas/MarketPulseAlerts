package com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoinGeckoCryptoDTO(
    String id,
    String symbol,
    String name,
    @JsonProperty("current_price") double currentPrice
) { }
