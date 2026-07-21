# Pequeño Morrison — servidor gRPC

Reescritura en Kotlin con Spring Boot, Spring Data JPA e Hibernate. Mantiene sin cambios los contratos `auth`, `clients` y `books` consumidos por la API REST.

## Requisitos

- Java 21
- Maven 3.6.3+
- PostgreSQL con el schema oficial de Pequeño Morrison
- Docker opcional

## Variables de entorno

| Variable | Predeterminado | Uso |
|---|---:|---|
| `GRPC_PORT` | `50051` | Puerto gRPC |
| `DB_HOST` | `localhost` | Host PostgreSQL |
| `DB_PORT` | `5432` | Puerto PostgreSQL |
| `DB_NAME` | `pequeno_morrison` | Base de datos |
| `DB_USER` | `pequeno_morrison` | Usuario |
| `DB_PASSWORD` | `pequeno_morrison` | Contraseña |
| `DB_POOL_SIZE` | `10` | Máximo de conexiones Hikari |
| `MIN_INITIAL_BALANCE_IN_CENTS` | `15000` | Saldo inicial mínimo |
| `MAX_INITIAL_BALANCE_IN_CENTS` | `1000000` | Saldo inicial máximo |
| `SESSION_DURATION` | `PT24H` | Duración ISO-8601 de sesión |

Hibernate usa `ddl-auto: validate`: no crea ni modifica tablas y detiene el arranque ante cualquier incompatibilidad con el schema.

## Ejecutar localmente

```bash
mvn clean package
java -jar target/morrison-grpc-2.0.0.jar
```

## Ejecutar con Docker

Con el contenedor `postgres` conectado a `morrison-network`:

```bash
docker network create morrison-network 2>/dev/null || true
docker compose up --build -d
docker logs -f morrison-grpc
```

La API REST ejecutada en macOS usa `GRPC_HOST=127.0.0.1`. Dentro de la misma red Docker usa `GRPC_HOST=morrison-grpc`.

## Errores de dominio

Los códigos se envían en la descripción del estado gRPC: `INVALID_SESSION`, `INVALID_CREDENTIALS`, `EMAIL_ALREADY_EXISTS`, `BOOK_NOT_FOUND`, `CLIENT_NOT_FOUND`, `INVALID_QUANTITY`, `INVALID_PRICE_FILTER`, `INSUFFICIENT_STOCK` e `INSUFFICIENT_BALANCE`.

## Consistencia de compras

`BuyBook` ejecuta una transacción y aplica bloqueo pesimista sobre cliente y libro. Dentro de la misma transacción valida saldo/stock, descuenta ambos y crea la factura con precio y título históricos.
