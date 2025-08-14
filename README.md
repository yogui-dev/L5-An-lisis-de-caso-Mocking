# FoodDeliveryX - Análisis de Mocking de OrderService

Este proyecto demuestra cómo aislar `OrderService` de dependencias externas (`PaymentService`, `CourierService`) usando pruebas unitarias con Mockito y JUnit 5.

Objetivos clave:
- Flujo feliz: pago exitoso + solicitud a courier.
- Manejo de errores: excepción de pago simulada, courier no disponible.
- Verificación de interacciones con `ArgumentCaptor`.
- Uso dirigido de `@Spy` para métodos internos puros.

## Requisitos
- Maven 3.9+
- JDK 21+ (Los tests también corren en 24. Para cobertura con JaCoCo, preferir JDK 21 por soporte de herramientas.)

## Estructura del proyecto
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
      ports/
        PaymentService.java
        CourierService.java
      application/
        OrderService.java
    test/java/com/fooddeliveryx/
      application/
        OrderServiceTest.java
      fixtures/
        OrderFixtures.java
    test/resources/
      mockito-extensions/
        org.mockito.plugins.MockMaker
```

## Cómo ejecutar
- Build rápido sin tests:
  ```bash
  mvn -q -DskipTests package
  ```
- Ejecutar pruebas unitarias:
  ```bash
  mvn -q test
  ```
- Pipeline local (tests + verify):
  ```bash
  mvn -q verify
  ```

## Cobertura (JaCoCo)
JaCoCo está configurado pero se omite por defecto (debido a brechas de soporte para Java 24).

- Para generar cobertura y aplicar 85% en `application` y `domain`, usar JDK 21 y ejecutar:
  ```bash
  mvn -q -Pcoverage verify
  ```
- Reporte: `target/site/jacoco/index.html`

Si usas JDK 24, ejecutar con `-Pcoverage` puede fallar hasta que JaCoCo añada soporte completo para Java 24.

## Mockito en JDK modernos
- Byte Buddy está fijado explícitamente para tests: `net.bytebuddy:byte-buddy:1.15.0` y `net.bytebuddy:byte-buddy-agent:1.15.0` para mejorar compatibilidad con JDK 24.
- Surefire está configurado con `argLine=-XX:+EnableDynamicAgentLoading` para silenciar el warning de carga dinámica del agente en JDK 24.
- Se fuerza el mock maker por subclase vía `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` para evitar problemas de instrumentación en JDK 24.

### Notas de ejecución (JDK 24)
- Los tests unitarios funcionan en JDK 24 con la configuración anterior; aún podrían aparecer algunos warnings de herramientas de terceros.
- La cobertura con JaCoCo está deshabilitada por defecto en JDK 24. Para cobertura, cambia a JDK 21 y ejecuta con `-Pcoverage`.

## VS Code
- Instala "Extension Pack for Java".
- Usa el Test Explorer para ejecutar pruebas.

## Extender pruebas
- Agrega escenarios de reintentos/idempotencia con `verify(..., times(N))`.
- Añade aserciones adicionales con AssertJ para mayor claridad.

## Notas
- Sin llamadas externas reales: todas las dependencias están mockeadas.
- Nombres en inglés en el código, comentarios breves en español donde ayude.
