package com.fooddeliveryx.fixtures;

import com.fooddeliveryx.domain.Order;

import java.math.BigDecimal;
import java.util.List;

public class OrderFixtures {
    public static Order validOrder() {
        return new Order(
                "O-1",
                List.of("Burger"),
                new BigDecimal("25000"),
                "test@example.com",
                "Av. Siempre Viva 123"
        );
    }
}
