# Reflexión Final

## Ventajas de aislar dependencias en pruebas unitarias
- Velocidad: los tests unitarios con mocks se ejecutan en milisegundos, habilitando feedback inmediato y ciclos TDD más cortos.
- Fiabilidad y determinismo: al eliminar servicios externos, desaparecen las intermitencias por red, relojes, cuotas y datos variables.
- Detección temprana de errores: los fallos de lógica se evidencian antes de integrar; se reduce el costo de arreglos tardíos.
- Diagnóstico claro: cuando falla un test, el foco está en nuestra lógica; no hay ruido de la infraestructura.
- Diseño mejorado: obliga a definir puertos/contratos y responsabilidades claras (arquitectura hexagonal), reduciendo acoplamientos.
- Cobertura efectiva: es más sencillo cubrir ramas de control y escenarios borde (excepciones, reintentos, timeouts simulados).
- Coste operativo menor: no se necesita levantar contenedores ni credenciales; menor fricción para nuevos miembros del equipo.
- Seguridad y cumplimiento: sin llamadas reales ni datos sensibles en entornos locales/CI.

## Dificultades al introducir mocking en equipos acostumbrados a integraciones reales
- Cambio cultural: la creencia “solo vale si llama al servicio real” es común; requiere pedagogía y ejemplos.
- Acoplamientos existentes: código monolítico o con lógica mezclada con IO hace más difícil extraer puertos y aislar lógica.
- Miedo a falsos positivos: temor a que los mocks maquillen errores; se mitiga con una pirámide de pruebas balanceada (unitarias + contract tests + algunas integraciones).
- Mantenimiento de fixtures: si no hay patrones, los datos de prueba se dispersan y duplican; resolver con builders/fixtures reutilizables.
- Sobre-mockeo: tests frágiles que verifican detalles de implementación; preferir verificar comportamientos/contratos y usar `ArgumentCaptor` sólo donde aporta valor.
- Falta de contratos formales: sin interfaces claras (puertos), el mocking se vuelve ad hoc; establecer interfaces y DTOs estables.
- Curva de herramientas: Mockito/AssertJ/JUnit 5 tienen su sintaxis y mejores prácticas; solución: plantillas, dojos y code reviews guiados.

## Cómo la adopción de mocks en IntelliJ Community ayuda al desarrollo seguro y continuo
- Entorno liviano: sin necesidad de Docker ni servicios externos para iterar; abrir el proyecto y correr tests en el gutter ▶.
- Productividad: ejecución selectiva de métodos de test, depuración con breakpoints y “Rerun Failed Tests” aceleran el ciclo.
- Estabilidad en CI: lo que corre localmente de forma determinista se replica igual en pipelines, minimizando flakes.
- Configuración clara: perfiles Maven (p. ej. `coverage`) y SDKs por proyecto (JDK 24 para desarrollo, JDK 21 para cobertura) facilitan el cambio de contexto.
- Seguridad: al no tocar servicios reales ni secretos, es más fácil cumplir políticas internas y desarrollar en ramas con confianza.
- Escalabilidad del equipo: nuevas personas pueden contribuir sin levantar infraestructura; los contratos de puertos actúan como documentación viva.

### Workflow recomendado en IntelliJ (Community)
1. Abrir el proyecto (Maven autoimport).
2. Configurar SDK: File > Project Structure > Project SDK (JDK 24 para desarrollo; JDK 21 para cobertura con `-Pcoverage`).
3. Correr tests desde el gutter o desde la Tool Window de Maven (`Lifecycle > test/verify`).
4. Para cobertura: activar el perfil `coverage` y ejecutar `verify` con JDK 21. Reporte en `target/site/jacoco/index.html`.
5. Usar fixtures y `@ExtendWith(MockitoExtension.class)` para tests consistentes; `@Spy` sólo cuando tenga sentido en métodos puros.

## Conclusión
Aislar dependencias con mocks no reemplaza todas las pruebas de integración, pero sí habilita una base de calidad rápida, determinista y mantenible. En combinación con IntelliJ Community y una arquitectura orientada a puertos, el equipo gana velocidad sin sacrificar seguridad ni precisión, fomentando un desarrollo continuo y seguro.
