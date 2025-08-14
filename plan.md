# Plan de aislamiento y pruebas de OrderService

## 1) Objetivo técnico

Aislar `OrderService` de sus dependencias externas (`PaymentService`, `CourierService`) usando mocks.

Validar:
- Flujo feliz de pago + solicitud de repartidor.
- Manejo de errores en pago (excepción simulada).
- Verificación de interacciones y `ArgumentCaptor`.
- Uso puntual de `Spy` cuando convenga.

---

## 2) Estructura inicial del proyecto

```
fooddeliveryx/
  pom.xml
  src/
    main/java/com/fooddeliveryx/
      domain/
        Order.java
        PaymentRequest.java
        PaymentResult.java
        CourierRequest.java
        CourierAssignment.java
        exceptions/
          PaymentRejectedException.java
      ports/           // Interfaces (contratos) hacia afuera
        PaymentService.java
        CourierService.java
      application/
        OrderService.java
    test/java/com/fooddeliveryx/
      application/
        OrderServiceTest.java
      fixtures/
        OrderFixtures.java
```

---

## 3) Dependencias (Maven)

Añadir a `pom.xml`:
- JUnit Jupiter (5.x)
- Mockito Core + Mockito JUnit Jupiter
- (Opcional) AssertJ para aserciones más expresivas

---

## 4) Modelado mínimo (Domain)

- `Order { id, items, totalAmount, customerEmail, deliveryAddress }`
- `PaymentRequest { orderId, amount, method }`
- `PaymentResult { success, transactionId, message }`
- `CourierRequest { orderId, address }`
- `CourierAssignment { courierId, etaMinutes }`
- `PaymentRejectedException extends RuntimeException`

---

## 5) Puertos (Interfaces)

- `PaymentService`: `PaymentResult charge(PaymentRequest request)`
- `CourierService`: `CourierAssignment requestCourier(CourierRequest request)`

---

## 6) Servicio de aplicación

`OrderService` con casos:
- `placeOrder(Order order)`: cobra y pide repartidor; retorna `CourierAssignment` o lanza excepción si el pago falla.
- (Opcional) `confirmPayment(...)`, `scheduleCourier(...)` si se quiere granularidad.

---

## 7) Plan de pruebas unitarias (JUnit + Mockito)

Crear casos atómicos, cada uno con Given/When/Then:

1) Happy path: pago OK + courier OK
- Mock `PaymentService.charge` → `PaymentResult(success=true, txId="...")`
- Mock `CourierService.requestCourier` → `CourierAssignment(courierId="C-1", eta=12)`
- Verificar:
  - `PaymentService.charge` invocado 1 vez con `orderId` y `amount` correctos (`ArgumentCaptor`).
  - `CourierService.requestCourier` invocado con `address` correcto (`ArgumentCaptor`).
  - Resultado del método retorna el `CourierAssignment` esperado.

2) Pago rechazado (excepción simulada)
- `when(paymentService.charge(...)).thenThrow(new PaymentRejectedException("Insufficient funds"))`
- `assertThrows(PaymentRejectedException, () -> orderService.placeOrder(order))`
- Verificar no interacción con `CourierService` (`verifyNoInteractions(courierService)`).

3) Courier no disponible (error controlado)
- Pago OK.
- `CourierService.requestCourier` lanza `RuntimeException("No couriers")`.
- Política a decidir:
  - Re-lanzar excepción, o
  - Retornar un “fallback”/estado pendiente (si se define tal contrato).
- Verificar mensaje/error y que el pago sí se intentó.

4) Validación de parámetros con `ArgumentCaptor`
- Capturar `PaymentRequest` y `CourierRequest` y asertar:
  - `orderId` coincide.
  - `amount` exacto (usa `BigDecimal` y compara por valor).
  - `address` coincide.

5) Uso de `Spy` (parcial)
- Conviene si `OrderService` tiene método interno “puro” (p. ej. `computeDeliveryWindow`) que se quiere invocar real, pero simulando llamadas a servicios externos.
- Crear `@Spy` sobre `OrderService` (o sobre un helper), `doReturn(value).when(spy).computeDeliveryWindow(...)`.
- Asertar que el resto del flujo sigue usando los mocks.

6) Idempotencia / reintentos (opcional)
- Si se agrega un mecanismo de reintento:
  - Verificar número exacto de invocaciones: `verify(paymentService, times(1)).charge(...)`, `verify(courierService, times(3)).requestCourier(...)`.

---

## 8) Criterios de aceptación por test

- AA: Arrange (datos y mocks), Act (ejecución), Assert (resultado e interacciones).
- Sin dependencias de red/IO.
- Nombres de métodos de test descriptivos (inglés):
  - `should_request_courier_when_payment_is_successful()`
  - `should_throw_when_payment_fails()`
  - `should_not_call_courier_if_payment_fails()`

---

## 9) Datos de prueba (Fixtures)

`OrderFixtures`:
- `validOrder()` con:
  - `totalAmount = 25_000`
  - `deliveryAddress = "Av. Siempre Viva 123"`
  - `customerEmail = "test@example.com"`
- Métodos helper para `PaymentRequest`/`CourierRequest` esperados.

---

## 10) Estándares de calidad

- Cobertura objetivo: ≥ 85% en `application` y `domain`.
- Mutation testing (opcional): PIT para asegurar robustez de tests.
- Checkstyle/SpotBugs (opcional) para estilo/bugs estáticos.

---

## 11) Automatización local

Scripts Maven:
- `mvn -q -DskipTests package` (build rápido)
- `mvn -q test` (tests)
- `mvn -q verify` (pipeline local)

VS Code:
- Extensión “Extension Pack for Java”.
- Ejecutar tests desde el Test Explorer.

---

## 12) Roadmap de implementación (mini-cronograma)

- Día 1
  - Crear proyecto y dependencias.
  - Modelado domain + ports.
  - Borrador `OrderService`.

- Día 2
  - Tests 1 y 2 (happy path y pago rechazado).
  - Incorporar `ArgumentCaptor`.

- Día 3
  - Test de courier no disponible + `Spy` parcial.
  - Refactor de `OrderService` (pequeños métodos puros para facilitar el spy).

- Día 4
  - Fixtures, limpieza, cobertura.
  - (Opcional) Mutation testing y reglas de estilo.

---

## 13) Definition of Done (DoD)

- Todos los tests pasan con ≥ 85% coverage.
- No hay llamadas reales a servicios externos (solo mocks).
- Casos de error y parámetros validados con `ArgumentCaptor`.
- Documentación breve del flujo (README con cómo correr tests).
- Código con nombres en inglés y comentarios breves en español donde aporte.
