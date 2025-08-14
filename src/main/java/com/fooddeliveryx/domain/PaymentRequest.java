package com.fooddeliveryx.domain;

import java.math.BigDecimal;

public class PaymentRequest {
    private final String orderId;
    private final BigDecimal amount;
    private final String method;

    public PaymentRequest(String orderId, BigDecimal amount, String method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }
}
