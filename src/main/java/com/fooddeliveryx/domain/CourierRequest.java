package com.fooddeliveryx.domain;

public class CourierRequest {
    private final String orderId;
    private final String address;

    public CourierRequest(String orderId, String address) {
        this.orderId = orderId;
        this.address = address;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAddress() {
        return address;
    }
}
