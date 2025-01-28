package com.tomas.market.pulse.alerts.api.dtos;

import com.tomas.market.pulse.alerts.model.MarketType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
    @NotNull(message = "Financial instrument symbol is required") String symbol,
    @NotNull(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotNull(message = "Upper threshold is required") @Min(value = 0, message = "Upper threshold must be non-negative") Integer upperThreshold,
    @NotNull(message = "Lower threshold is required") @Min(value = 0, message = "Lower threshold must be non-negative") Integer lowerThreshold,
    @NotNull(message = "MarketType is required") MarketType marketType
    ) {
}