# Resumen técnico de la prueba de precios

*Documento original: 12 de septiembre de 2023 — Revisado y actualizado: 26 de junio de 2026*

## Descripción general

El objetivo del proyecto es construir una API RESTful con Spring Boot para gestionar precios de productos en una cadena de tiendas. La API recibe una fecha de aplicación, un identificador de producto y un identificador de cadena, y devuelve el precio aplicable resolviendo conflictos por prioridad (gana la tarifa de mayor prioridad para el rango de fechas).

El proyecto nació en 2023 sobre Spring Boot 2.7 y una arquitectura en capas. En 2026 se ha modernizado por completo: arquitectura hexagonal, Java 25, Spring Boot 3.5 y un conjunto de mejoras de calidad, observabilidad y rendimiento que se detallan más abajo.

## Arquitectura

El código sigue una **arquitectura hexagonal (Ports & Adapters)** bajo el paquete `com.priceservice.app`, con la regla de dependencias `infrastructure → application → domain`:

- **domain** — núcleo de negocio sin frameworks: el modelo `Price` (POJO), el value object `ApplicablePrices` que encapsula la regla de prioridad (`highestPriority()`) y las excepciones de negocio (`PriceNotFoundException`, `BrandNotFoundException`, `ProductNotFoundException`).
- **application** — el caso de uso `GetPriceUseCase` (puerto de entrada) con su `GetPriceQuery`, el puerto de salida `LoadPricePort` y el servicio `GetPriceService`, que orquesta el flujo y delega la regla de negocio en el dominio.
- **infrastructure** — adaptadores: entrada web (`PriceController`, `PriceWebMapper`, `GlobalExceptionHandler`, filtros), salida de persistencia (`PriceJpaEntity`, `PriceJpaRepository`, `PricePersistenceAdapter`, `PricePersistenceMapper`) y configuración (`BeanConfiguration`, `CacheConfiguration`).

La decisión original de usar una arquitectura N-Layer "por simplicidad" se ha sustituido por hexagonal para desacoplar el dominio de Spring, JPA y HTTP, permitir tests del caso de uso con mocks del puerto de salida y poder cambiar persistencia o capa de entrada sin tocar el núcleo. Las capas `domain` y `application` no contienen anotaciones de Spring: el wiring se declara en `infrastructure/config/BeanConfiguration`.

## Características técnicas y funcionales

- **Enfoque API-First:** el contrato `openapi.yaml` se diseña primero y `openapi-generator` genera la API e interfaces/DTOs durante el build.
- **Desarrollo guiado por comportamiento (BDD):** escenarios Gherkin con Cucumber, ejecutados sobre el **JUnit Platform**.
- **API orientada a recursos y versionada:** `GET /v1/brands/{brandId}/products/{productId}/price?date=...` (identificadores en el path, fecha como filtro).
- **Errores estándar RFC 7807:** las excepciones se traducen a `ProblemDetail` (`application/problem+json`, nativo en Spring 6), con `correlationId` y `timestamp` como extensiones.
- **Trazabilidad:** filtro de `correlation id` por petición.
- **Validación declarativa:** restricciones en el contrato (`minimum`, `required`) que el generador traduce a Bean Validation; las violaciones las maneja el `@RestControllerAdvice` devolviendo `400`.
- **Caché HTTP:** `Cache-Control` (max-age) y `ETag`/`If-None-Match` (304) en el endpoint, ya que el precio es estable.
- **Importe con `BigDecimal`:** el precio se modela con `BigDecimal` (no `double`) para evitar errores de redondeo monetario.
- **Caché de consulta:** la consulta de precios se cachea con Caffeine (TTL y tamaño máximo) en el adaptador de persistencia, sin acoplar el núcleo.
- **Rate limiting:** limitador token-bucket por IP sobre `/v1/**`, configurable y desactivable; responde `429 Too Many Requests` al superar el límite.
- **Colección Postman:** incluida para explorar y probar el servicio.

## Mejoras de calidad y operación

- **Logging con SLF4J.**
- **Pruebas unitarias con JUnit 5 y Mockito** sobre el caso de uso y los mappers.
- **Test de slice web con MockMvc** (`@WebMvcTest`) que cubre los caminos 200 / 404 / 400.
- **Test de integración full-stack** (`@SpringBootTest` con puerto real) que entra por HTTP hasta H2 cubriendo los 5 escenarios del enunciado + un error.
- **Tests de integración BDD** (Cucumber sobre contexto Spring + H2) para los escenarios de negocio.
- **Tests de arquitectura con ArchUnit:** verifican la direccionalidad de capas y que `domain`/`application` no se acoplen a Spring ni a JPA.
- **Cobertura con JaCoCo:** informe generado en `verify` (`target/site/jacoco/index.html`).
- **Pruebas de carga con Gatling** (perfil Maven aparte) sobre el endpoint de precio, con aserciones de p95 y porcentaje de éxito.
- **Análisis estático con SonarQube:** revisado y sin issues abiertas (bugs, code smells y nulabilidad de overrides corregidos).
- **Observabilidad:** Spring Boot Actuator + métricas Prometheus (`/actuator/health`, `/actuator/prometheus`).
- **Build reproducible:** codificación UTF-8 fijada en `.mvn/jvm.config` para que no dependa de la configuración local.
- **Dockerización:** `Dockerfile` multi-stage (build + runtime JRE 25, usuario no root) y `docker-compose`.

## Tecnologías utilizadas

- Java 25
- Spring Boot 3.5.x
- H2 Database (in-memory)
- JUnit 5, Mockito
- Gherkin / Cucumber (JUnit Platform)
- ArchUnit (tests de arquitectura)
- JaCoCo (cobertura)
- Gatling (pruebas de carga)
- springdoc-openapi (Swagger UI) + openapi-generator
- Micrometer + Prometheus (Actuator)
- Caffeine (caché en memoria)
- Lombok
- Docker
- SLF4J
- Postman

## Mejoras contempladas fuera del alcance

Las siguientes mejoras se han valorado pero **no se han implementado por quedar fuera del alcance de la prueba**. Se documentan porque conviene conocer su existencia y porque demuestran por dónde evolucionaría el servicio en un contexto real:

- **Integración y despliegue continuos (CI/CD):** pipeline que ejecute `mvn clean verify` con JDK 25 y, opcionalmente, un job de Gatling.
- **Resiliencia (Resilience4j):** circuit breaker, retry con backoff y timeout en el acceso a datos *cuando la BD deje de ser H2 en memoria y pase a ser remota* o se llamen otros servicios. Hoy no aplica (H2 local no falla ni tiene latencia de red).
- **Persistencia real:** sustituir H2 por una BD (p. ej. PostgreSQL) con migraciones (Flyway/Liquibase), índice en `(product_id, brand_id, start_date, end_date)` y tests con Testcontainers.
- **Autenticación y autorización:** JWT para restringir el acceso (`SecurityConfiguration`).
- **Caché y rate limiting distribuidos:** migrar de Caffeine/limitador en memoria a Redis cuando haya varias instancias (el límite actual es por instancia).
- **Trazabilidad distribuida:** Micrometer Tracing + OpenTelemetry propagando el correlation id como `traceId`; logs estructurados en JSON.
- **Internacionalización (i18n):** soporte de idiomas y formatos de moneda en mensajes.
- **Balanceo de carga:** distribución de tráfico entre instancias.
