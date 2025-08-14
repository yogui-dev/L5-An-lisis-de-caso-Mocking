package com.fooddeliveryx.application;

import com.fooddeliveryx.domain.CourierAssignment;
import com.fooddeliveryx.domain.CourierRequest;
import com.fooddeliveryx.domain.Order;
import com.fooddeliveryx.domain.PaymentRequest;
import com.fooddeliveryx.domain.PaymentResult;
import com.fooddeliveryx.domain.exceptions.PaymentRejectedException;
import com.fooddeliveryx.fixtures.OrderFixtures;
import com.fooddeliveryx.ports.CourierService;
import com.fooddeliveryx.ports.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private PaymentService paymentService;
    private CourierService courierService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        paymentService = Mockito.mock(PaymentService.class);
        courierService = Mockito.mock(CourierService.class);
        orderService = Mockito.spy(new OrderService(paymentService, courierService));
    }

    @Test
    void should_request_courier_when_payment_is_successful() {
        // Arrange
        Order order = OrderFixtures.validOrder();
        Mockito.when(paymentService.charge(Mockito.any()))
                .thenReturn(new PaymentResult(true, "TX-123", "OK"));
        Mockito.when(courierService.requestCourier(Mockito.any()))
                .thenReturn(new CourierAssignment("C-1", 12));

        // Act
        CourierAssignment result = orderService.placeOrder(order);

        // Assert: resultado
        assertThat(result).isNotNull();
        assertThat(result.getCourierId()).isEqualTo("C-1");
        assertThat(result.getEtaMinutes()).isEqualTo(12);

        // Assert: interacciones y argumentos
        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        Mockito.verify(paymentService, Mockito.times(1)).charge(paymentCaptor.capture());
        PaymentRequest capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getOrderId()).isEqualTo(order.getId());
        assertThat(capturedPayment.getAmount()).isEqualByComparingTo(order.getTotalAmount());

        ArgumentCaptor<CourierRequest> courierCaptor = ArgumentCaptor.forClass(CourierRequest.class);
        Mockito.verify(courierService, Mockito.times(1)).requestCourier(courierCaptor.capture());
        CourierRequest capturedCourier = courierCaptor.getValue();
        assertThat(capturedCourier.getOrderId()).isEqualTo(order.getId());
        assertThat(capturedCourier.getAddress()).isEqualTo(order.getDeliveryAddress());
    }

    @Test
    void should_throw_when_payment_fails() {
        // Arrange
        Order order = OrderFixtures.validOrder();
        Mockito.when(paymentService.charge(Mockito.any()))
                .thenThrow(new PaymentRejectedException("Insufficient funds"));

        // Act + Assert
        assertThrows(PaymentRejectedException.class, () -> orderService.placeOrder(order));
        Mockito.verifyNoInteractions(courierService);
    }

    @Test
    void should_propagate_error_when_courier_unavailable() {
        // Arrange
        Order order = OrderFixtures.validOrder();
        Mockito.when(paymentService.charge(Mockito.any()))
                .thenReturn(new PaymentResult(true, "TX-1", "OK"));
        Mockito.when(courierService.requestCourier(Mockito.any()))
                .thenThrow(new RuntimeException("No couriers"));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.placeOrder(order));
        assertThat(ex.getMessage()).contains("No couriers");
        Mockito.verify(paymentService, Mockito.times(1)).charge(Mockito.any());
    }

    @Test
    void should_apply_spy_when_overriding_computeDeliveryWindow() {
        // Arrange
        Order order = OrderFixtures.validOrder();
        Mockito.when(paymentService.charge(Mockito.any()))
                .thenReturn(new PaymentResult(true, "TX-999", "OK"));
        Mockito.when(courierService.requestCourier(Mockito.any()))
                .thenReturn(new CourierAssignment("C-42", 12));

        // Spy: forzamos ventana de entrega calculada a 20 minutos
        Mockito.doReturn(20).when(orderService).computeDeliveryWindow(Mockito.eq(order), Mockito.eq(12));

        // Act
        CourierAssignment result = orderService.placeOrder(order);

        // Assert
        assertThat(result.getCourierId()).isEqualTo("C-42");
        assertThat(result.getEtaMinutes()).isEqualTo(20);
        Mockito.verify(paymentService, Mockito.times(1)).charge(Mockito.any());
        Mockito.verify(courierService, Mockito.times(1)).requestCourier(Mockito.any());
    }
}
