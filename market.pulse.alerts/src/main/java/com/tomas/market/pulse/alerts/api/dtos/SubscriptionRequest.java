package com.tomas.market.pulse.alerts.api.dtos;

import com.tomas.market.pulse.alerts.model.MarketType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
    @NotNull(message = "Financial instrument ID is required") String financialInstrumentId,
    @NotNull(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotNull(message = "Upper threshold is required") @Min(value = 0, message = "Upper threshold must be non-negative") Integer upperThreshold,
    @NotNull(message = "Lower threshold is required") @Min(value = 0, message = "Lower threshold must be non-negative") Integer lowerThreshold,
    @NotNull(message = "Original price is required") @Min(value = 0, message = "Original price must be non-negative") Double originalPrice,
    @NotNull(message = "Current price is required") @Min(value = 0, message = "Current price must be non-negative") Double currentPrice,
    @NotNull(message = "MarketType is required") MarketType marketType
    ) {
}