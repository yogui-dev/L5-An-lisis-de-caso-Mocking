package com.fooddeliveryx.application;

import com.fooddeliveryx.domain.CourierAssignment;
import com.fooddeliveryx.domain.CourierRequest;
import com.fooddeliveryx.domain.Order;
import com.fooddeliveryx.domain.PaymentRequest;
import com.fooddeliveryx.domain.PaymentResult;
import com.fooddeliveryx.domain.exceptions.PaymentRejectedException;
import com.fooddeliveryx.ports.CourierService;
import com.fooddeliveryx.ports.PaymentService;

public class OrderService {
    private final PaymentService paymentService;
    private final CourierService courierService;

    public OrderService(PaymentService paymentService, CourierService courierService) {
        this.paymentService = paymentService;
        this.courierService = courierService;
    }

    public CourierAssignment placeOrder(Order order) {
        PaymentRequest paymentRequest = new PaymentRequest(order.getId(), order.getTotalAmount(), "CARD");
        PaymentResult paymentResult = paymentService.charge(paymentRequest);

        if (paymentResult == null) {
            throw new PaymentRejectedException("Payment result is null");
        }
        if (!paymentResult.isSuccess()) {
            throw new PaymentRejectedException(paymentResult.getMessage() != null ? paymentResult.getMessage() : "Payment rejected");
        }

        CourierRequest courierRequest = new CourierRequest(order.getId(), order.getDeliveryAddress());
        CourierAssignment assignment = courierService.requestCourier(courierRequest);

        int eta = computeDeliveryWindow(order, assignment.getEtaMinutes());
        return new CourierAssignment(assignment.getCourierId(), eta);
    }

    // Método puro para facilitar Spy en tests
    protected int computeDeliveryWindow(Order order, int baseEtaMinutes) {
        return baseEtaMinutes; // comportamiento por defecto: no altera ETA
    }
}
