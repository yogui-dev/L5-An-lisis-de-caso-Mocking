package com.fooddeliveryx.ports;

import com.fooddeliveryx.domain.CourierAssignment;
import com.fooddeliveryx.domain.CourierRequest;

public interface CourierService {
    CourierAssignment requestCourier(CourierRequest request);
}
