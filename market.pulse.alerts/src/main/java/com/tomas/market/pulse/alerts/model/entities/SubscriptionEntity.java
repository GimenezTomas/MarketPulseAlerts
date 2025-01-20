package com.tomas.market.pulse.alerts.model.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "subscriptions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "financial_instrument_id", nullable = false)
    private FinancialInstrumentEntity financialInstrument;

    @NotNull
    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "upper_threshold", nullable = false)
    private int upperThreshold;

    @NotNull
    @Column(name = "lower_threshold", nullable = false)
    private int lowerThreshold;

    @NotNull
    @Column(name = "original_price", nullable = false)
    private double originalPrice;

    @NotNull
    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @PrePersist
    private void initializeCreatedOn() {
        this.createdOn = Instant.now();
    }
}
