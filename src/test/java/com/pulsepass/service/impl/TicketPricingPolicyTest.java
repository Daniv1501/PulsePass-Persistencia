package com.pulsepass.service.impl;

import com.pulsepass.domain.TicketType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de la estrategia de precio (PRD sección 28: "encapsulada y probada").
 * Es una función pura, así que no necesita mocks. Vive en el mismo paquete que
 * la política porque ésta es package-private.
 */
class TicketPricingPolicyTest {

    @Test
    void generalTicketCostsBasePrice() {
        assertThat(TicketPricingPolicy.calculatePrice(TicketType.GENERAL))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void studentTicketHasDiscount() {
        assertThat(TicketPricingPolicy.calculatePrice(TicketType.STUDENT))
                .isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void vipTicketUsesMultiplier() {
        assertThat(TicketPricingPolicy.calculatePrice(TicketType.VIP))
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void backstageTicketUsesHigherMultiplierThanVip() {
        BigDecimal backstage = TicketPricingPolicy.calculatePrice(TicketType.BACKSTAGE);

        assertThat(backstage).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(backstage).isGreaterThan(TicketPricingPolicy.calculatePrice(TicketType.VIP));
    }

    // BR-TICKET-009: nunca price < 0, para todos los tipos
    @ParameterizedTest
    @EnumSource(TicketType.class)
    void priceIsNeverNegative(TicketType type) {
        assertThat(TicketPricingPolicy.calculatePrice(type).signum()).isPositive();
    }

    @ParameterizedTest
    @EnumSource(TicketType.class)
    void pricingIsDeterministic(TicketType type) {
        assertThat(TicketPricingPolicy.calculatePrice(type))
                .isEqualByComparingTo(TicketPricingPolicy.calculatePrice(type));
    }
}
