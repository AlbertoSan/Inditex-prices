# Prueba Técnica Precios Text

## Descripción General

El objetivo de esta prueba es construir una API en Spring Boot para calcular el precio final de un producto en una cadena de tiendas para una fecha específica. El proyecto utiliza las siguientes tecnologías y enfoques:

- Java 25 y Spring Boot 3.5.x
- H2 Database
- Docker
- SLF4J para Logging
- Gherkin y BDD (Cucumber)
- springdoc-openapi (Swagger UI) + generación de contrato con openapi-generator
- Postman

---

## Arquitectura (Hexagonal / Ports & Adapters)

El código está organizado en tres capas concéntricas bajo `com.priceservice.app`:

```
domain/                         Núcleo de negocio, sin frameworks
  model/Price                   Modelo de dominio (POJO, sin JPA)
  exception/                    Excepciones de negocio

application/                    Orquestación del caso de uso
  port/in/GetPriceUseCase       Puerto de entrada (driving)
  port/in/GetPriceQuery         Objeto de consulta del caso de uso
  port/out/LoadPricePort        Puerto de salida (driven)
  service/GetPriceService       Implementa el caso de uso + regla de prioridad

infrastructure/                 Adaptadores (detalles técnicos)
  adapter/in/web/               REST: PriceController, PriceWebMapper,
                                GlobalExceptionHandler, filtros
  adapter/out/persistence/      JPA: PriceJpaEntity, PriceJpaRepository,
                                PricePersistenceAdapter, PricePersistenceMapper
```

Regla de dependencias: `infrastructure → application → domain`. El dominio no
conoce Spring, JPA ni HTTP; la lógica de negocio depende sólo de puertos
(interfaces) y los adaptadores los implementan. Esto permite sustituir la
persistencia o la capa de entrada sin tocar el núcleo, y probar el caso de uso
con mocks del puerto de salida.

---

## Instalación y Configuración

### Requisitos Previos

- JDK 25 o superior
- Maven 3.9+ (o generar el wrapper con `mvn wrapper:wrapper`)
- Docker (opcional, para ejecución en contenedor)

La aplicación escucha en el puerto **8080** (configurable con la variable de
entorno `SERVER_PORT`).

### Pasos para la Instalación y Ejecución

1. **Construir y empaquetar con Maven**
    ```bash
    mvn clean package
    ```
    Genera `target/ms-price-service.jar`.

2. **Ejecutar el proyecto**
    - Con el JAR generado:
        ```bash
        java -jar target/ms-price-service.jar
        ```
    - Con Docker (build multi-stage, no requiere `mvn package` previo):
        ```bash
        docker build -t ms-price-service:0.0.1-SNAPSHOT .
        docker run -p 8080:8080 ms-price-service:0.0.1-SNAPSHOT
        ```
    - Con Docker Compose:
        ```bash
        docker compose up --build
        ```

3. **Ejecutar las pruebas**
    ```bash
    mvn test
    ```

### Endpoints útiles

- API: `GET http://localhost:8080/v1/brands/1/products/35455/price?date=2020-06-14T17:12:30.174Z`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Consola H2: `http://localhost:8080/h2-console`
- Health: `http://localhost:8080/actuator/health`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`

### Rate limiting

El endpoint `/v1/**` está protegido por un rate limiter de token-bucket por IP,
configurable en `application.properties`:

```
ratelimit.enabled=true
ratelimit.capacity=5
ratelimit.refill-tokens=5
ratelimit.refill-period-seconds=1
```

Supera el límite y devuelve `429 Too Many Requests`. Desactívalo con
`ratelimit.enabled=false` (necesario para las pruebas de carga).

### Pruebas de carga (Gatling)

La simulación vive en `src/gatling/java` y se ejecuta en un perfil aparte, sin
afectar al build normal:

```bash
# 1) Start the app without rate limiting
java -jar target/ms-price-service.jar --ratelimit.enabled=false
# 2) Compile and run the simulation (Gatling profile)
mvn -Pgatling test-compile gatling:test
```

Incluye aserciones de p95 < 800 ms y > 99% de peticiones correctas.

