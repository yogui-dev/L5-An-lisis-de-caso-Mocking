# Informe Escrito — Aislamiento de OrderService con Mocks

Este informe resume los servicios externos mockeados, cómo su simulación acelera las pruebas unitarias, e incluye fragmentos de código que ejemplifican la inyección de mocks, la verificación de interacciones, y el manejo de excepciones simuladas con el uso de ArgumentCaptor y Spies.

## 1) Servicios externos mockeados y beneficios

### PaymentService (puerto)
- Contrato: `PaymentResult charge(PaymentRequest request)`
- Posibles errores: `PaymentRejectedException` (pago rechazado)
- Por qué se mockea:
  - Evita llamadas a pasarelas reales (latencia, cuotas, credenciales).
  - Tests deterministas: control total del resultado (aprobado/rechazado).
  - Permite cubrir escenarios raros (rechazos específicos) sin depender de datos del proveedor.

### CourierService (puerto)
- Contrato: `CourierAssignment requestCourier(CourierRequest request)`
- Posibles errores: indisponibilidad de couriers (ej. `RuntimeException`)
- Por qué se mockea:
  - Evita dependencias de disponibilidad real de mensajería.
  - Permite simular fallas operativas para validar política de reintentos o propagación de errores.

Beneficio transversal de la simulación:
- Velocidad: ejecución en milisegundos, sin IO ni red.
- Fiabilidad: sin “flaky tests” causados por entornos externos.
- Cobertura: fácil forzar ramas de error y condiciones límite.
- Seguridad: sin uso de secretos ni datos sensibles locales/CI.

## 2) Inyección de mocks y verificación de interacciones (código)

### Inyección básica por constructor
```java
import org.mockito.Mockito;
import com.fooddeliveryx.application.OrderService;
import com.fooddeliveryx.ports.PaymentService;
import com.fooddeliveryx.ports.CourierService;

PaymentService paymentService = Mockito.mock(PaymentService.class);
CourierService courierService = Mockito.mock(CourierService.class);

OrderService orderService = new OrderService(paymentService, courierService);
```

### Stubbing + verificación con ArgumentCaptor
```java
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;

// Arrange
Order order = OrderFixtures.validOrder();
PaymentResult ok = new PaymentResult(true, "APPROVED", "txn-123");
when(paymentService.charge(any())).thenReturn(ok);
when(courierService.requestCourier(any())).thenReturn(new CourierAssignment("cour-1", 15));

// Act
var result = orderService.placeOrder(order);

// Assert
ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
verify(paymentService, times(1)).charge(paymentCaptor.capture());
PaymentRequest sent = paymentCaptor.getValue();
assertThat(sent.orderId()).isEqualTo(order.id());
assertThat(sent.amount()).isEqualTo(order.totalAmount());

verify(courierService, times(1)).requestCourier(any(CourierRequest.class));
assertThat(result.paymentTransactionId()).isEqualTo("txn-123");
```

### Verificación de no-interacción cuando falla el pago
```java
import static org.junit.jupiter.api.Assertions.assertThrows;

when(paymentService.charge(any())).thenThrow(new PaymentRejectedException("REJECTED"));

assertThrows(PaymentRejectedException.class, () -> orderService.placeOrder(order));

// No debe solicitar courier si el pago falla
verifyNoInteractions(courierService);
```

## 3) Manejo de excepciones simuladas y uso de ArgumentCaptor/Spies

### Excepción en el servicio de courier (propagación controlada)
```java
when(paymentService.charge(any())).thenReturn(new PaymentResult(true, "APPROVED", "txn-456"));
when(courierService.requestCourier(any())).thenThrow(new RuntimeException("No couriers"));

RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.placeOrder(order));
assertThat(ex.getMessage()).contains("No couriers");

// Se cobró exactamente una vez antes de fallar en courier
verify(paymentService, times(1)).charge(any());
```

### ArgumentCaptor para validar parámetros críticos
```java
ArgumentCaptor<CourierRequest> courierCaptor = ArgumentCaptor.forClass(CourierRequest.class);
verify(courierService).requestCourier(courierCaptor.capture());
CourierRequest req = courierCaptor.getValue();
assertThat(req.address()).isEqualTo(order.address());
assertThat(req.distanceKm()).isEqualTo(order.distanceKm());
```

### Uso de Spy para métodos internos puros
En `OrderService` existe un método interno puro (por ejemplo `computeDeliveryWindow(int distanceKm)`). Un `@Spy` permite estabilizar cálculos internos (p. ej. ventanas de entrega) sin alterar la lógica pública.

```java
import org.mockito.Spy;
import static org.mockito.Mockito.*;

OrderService spyService = Mockito.spy(new OrderService(paymentService, courierService));

when(paymentService.charge(any())).thenReturn(new PaymentResult(true, "APPROVED", "txn-789"));
when(courierService.requestCourier(any())).thenReturn(new CourierAssignment("cour-9", 30));

doReturn(20).when(spyService).computeDeliveryWindow(eq(order.distanceKm()));

var result = spyService.placeOrder(order);
assertThat(result.etaMinutes()).isEqualTo(20); // se respeta el valor fijado por el spy

verify(paymentService).charge(any());
verify(courierService).requestCourier(any());
```

Notas prácticas:
- Preferir Spy solo para métodos internos puros; evitar espiar lógica con efectos secundarios.
- Limitar verificaciones a comportamientos/contratos; no sobreespecificar detalles de implementación (tests frágiles).

## Conclusión
La simulación de `PaymentService` y `CourierService` con Mockito habilita tests rápidos, deterministas y con alta cobertura, facilitando validar rutas de error y contratos de integración sin depender de infraestructura externa. El uso de `ArgumentCaptor` verifica la corrección de los datos enviados a los puertos, y los `Spies` ofrecen un mecanismo acotado para estabilizar comportamientos internos puros cuando se requiere control fino del entorno de ejecución.
