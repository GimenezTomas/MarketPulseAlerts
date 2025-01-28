package com.tomas.market.pulse.alerts.clients.market.crypto.coin_gecko;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoinGeckoCryptoDTO(
    String id,
    String symbol,
    String name,
    @JsonProperty("current_price") double currentPrice,
    @JsonProperty("high_24h") double high24h,
    @JsonProperty("low_24h") double low24h
) { }
