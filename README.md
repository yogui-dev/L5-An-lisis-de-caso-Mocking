# FoodDeliveryX - OrderService Mocking Analysis

This project demonstrates how to isolate `OrderService` from external dependencies (`PaymentService`, `CourierService`) using unit tests with Mockito and JUnit 5.

Key goals:
- Happy-path: successful payment + courier request.
- Error handling: simulated payment exception, courier unavailable.
- Interaction verification with `ArgumentCaptor`.
- Targeted use of `@Spy` for pure internal methods.

## Requirements
- Maven 3.9+
- JDK 21+ (Tests run on 24 too. For coverage via JaCoCo, prefer JDK 21 due to tool support.)

## Project layout
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

## How to run
- Fast build without tests:
  ```bash
  mvn -q -DskipTests package
  ```
- Run unit tests:
  ```bash
  mvn -q test
  ```
- Local pipeline (tests + verify phase):
  ```bash
  mvn -q verify
  ```

## Code coverage (JaCoCo)
JaCoCo is configured but skipped by default (due to current Java 24 class format support gaps).

- To generate coverage and enforce 85% on `application` and `domain`, use JDK 21 and run:
  ```bash
  mvn -q -Pcoverage verify
  ```
- Reports: `target/site/jacoco/index.html`

If using JDK 24, running with `-Pcoverage` may fail until JaCoCo adds Java 24 support.

## Mockito on modern JDKs
- Byte Buddy is explicitly pinned for tests: `net.bytebuddy:byte-buddy:1.15.0` and `net.bytebuddy:byte-buddy-agent:1.15.0` to improve compatibility with JDK 24.
- Surefire is configured with `argLine=-XX:+EnableDynamicAgentLoading` to silence the dynamic agent warning on JDK 24.
- We force the subclass mock maker in tests via `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` to avoid instrumentation issues on JDK 24.

### Runtime notes (JDK 24)
- Unit tests run fine on JDK 24 with the above settings; you may still see some warnings from third-party tooling.
- Coverage via JaCoCo is disabled by default on JDK 24. For coverage, switch to JDK 21 and run with `-Pcoverage`.

## VS Code
- Install "Extension Pack for Java".
- Use Test Explorer to run tests.

## Extending tests
- Add retry/idempotency scenarios with `verify(..., times(N))`.
- Add extra assertions with AssertJ for clarity.

## Notes
- No real external calls: all dependencies are mocked.
- English naming in code, short Spanish comments where helpful.
