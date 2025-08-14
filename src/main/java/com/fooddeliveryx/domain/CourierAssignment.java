package com.fooddeliveryx.domain;

public class CourierAssignment {
    private final String courierId;
    private final int etaMinutes;

    public CourierAssignment(String courierId, int etaMinutes) {
        this.courierId = courierId;
        this.etaMinutes = etaMinutes;
    }

    public String getCourierId() {
        return courierId;
    }

    public int getEtaMinutes() {
        return etaMinutes;
    }
}
