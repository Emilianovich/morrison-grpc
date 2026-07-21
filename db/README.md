# Pequeño Morrison — PostgreSQL

Imagen personalizada de PostgreSQL con el esquema, índices y 75 libros semilla. No contiene triggers de lógica de negocio.

La imagen ya incluye estos valores:

```text
Database: pequeno_morrison
User: pequeno_morrison
Password: pequeno_morrison
Port: 5432
```

## Construir la imagen

```bash
docker build -t pequeno-morrison-postgres .
```

## Ejecutar

```bash
docker run -d -p 5432:5432 pequeno-morrison-postgres
```

Docker crea automáticamente un volumen anónimo para `/var/lib/postgresql`, por lo que no es necesario escribir `-v`.

## Conexión

```text
Host: localhost
Port: 5432
Database: pequeno_morrison
User: pequeno_morrison
Password: pequeno_morrison
```

## Detener y volver a iniciar

Obtén el identificador o nombre generado por Docker:

```bash
docker ps
```

Después puedes usar:

```bash
docker stop <contenedor>
docker start <contenedor>
```

## Datos iniciales

Los scripts dentro de `docker-entrypoint-initdb.d` se ejecutan únicamente cuando PostgreSQL inicializa un volumen vacío. Incluyen:

- Tablas y relaciones.
- Restricciones de integridad.
- Índices de búsqueda.
- 15 categorías.
- 75 libros semilla.

PostgreSQL valida claves foráneas, unicidad, valores no negativos y fechas de sesión. Todas las cantidades monetarias se almacenan como `BIGINT` en centavos:

- `clients.money_amount_in_cents`
- `books.price_in_cents`
- `invoices.unit_price_in_cents`
- `invoices.total_in_cents`

Los identificadores de clientes, autores, libros, sesiones y facturas son UUID; `categories.id` es un entero autogenerado. El servicio gRPC debe validar saldo y stock, descontar inventario, calcular el total y crear la factura dentro de una transacción.
