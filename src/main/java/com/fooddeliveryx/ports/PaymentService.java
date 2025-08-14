package com.fooddeliveryx.ports;

import com.fooddeliveryx.domain.PaymentRequest;
import com.fooddeliveryx.domain.PaymentResult;

public interface PaymentService {
    PaymentResult charge(PaymentRequest request);
}
