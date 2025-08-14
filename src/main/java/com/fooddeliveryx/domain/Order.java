package com.fooddeliveryx.domain;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private final String id;
    private final List<String> items;
    private final BigDecimal totalAmount;
    private final String customerEmail;
    private final String deliveryAddress;

    public Order(String id, List<String> items, BigDecimal totalAmount, String customerEmail, String deliveryAddress) {
        this.id = id;
        this.items = items;
        this.totalAmount = totalAmount;
        this.customerEmail = customerEmail;
        this.deliveryAddress = deliveryAddress;
    }

    public String getId() {
        return id;
    }

    public List<String> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }
}
