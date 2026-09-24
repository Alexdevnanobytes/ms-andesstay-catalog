# ms-andesstay-catalog

Microservicio de catálogo de AndesStay. Administra las unidades alojables (habitaciones y cabañas), informa su disponibilidad por fechas y registra las asignaciones que le piden los demás servicios (por ejemplo, `ms-andesstay-reservations`).

- **Puerto por defecto:** 8082
- **Stack:** Java 17, Spring Boot 3.4, Spring Data JPA, PostgreSQL (AWS RDS)
- **Tablas:** `as_units` y `as_allocations` (las crea Hibernate con `ddl-auto: update`, solo para demostración)

## Autenticación interna

Las rutas se consumen con el header `X-Internal-Token`, cuyo valor debe coincidir con la variable `INTERNAL_TOKEN`. Sin el header, o con un valor distinto, el servicio responde `401`.

Reservations envía este header en sus llamadas, por lo que **ambos servicios deben usar el mismo token**.

## Configuración

| Variable | Obligatoria | Por defecto | Descripción |
|---|---|---|---|
| `CATALOG_DB_PASSWORD` | Sí | (vacío) | Contraseña de la base de datos |
| `INTERNAL_TOKEN` | Sí | (vacío) | Token compartido entre servicios. Si falta, la app no arranca |
| `CATALOG_DB_USER` | No | `postgres` | Usuario de la base |
| `DB_URL` | No | URL de la base `andesstay` en RDS | Cadena de conexión JDBC |
| `PORT` | No | `8082` | Puerto HTTP |

La contraseña y el token no se guardan en el repositorio: se definen como variables de entorno.

## Cómo ejecutarlo

```powershell
$env:CATALOG_DB_PASSWORD="<contraseña>"
$env:INTERNAL_TOKEN="<token compartido>"
mvn spring-boot:run
```

Pruebas automáticas:

```powershell
mvn test
```

## Modelos

**Unidad**

```json
{
  "id": "c61709c9-c1f7-4eff-aebb-85c91b2537e8",
  "code": "H-101",
  "type": "HABITACION",
  "description": "Habitacion de prueba",
  "nightlyRate": 50000,
  "active": true
}
```

- `type`: `HABITACION` o `CABANA`.
- `code` es único.
- `nightlyRate` debe ser al menos `0.01`.

**Asignación:** vincula una reserva con una unidad para un rango de fechas. Se identifica por el `reservationId`.

## Endpoints públicos

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/catalog/units` | no lleva | `200` lista de unidades |
| GET | `/api/catalog/units/{id}` | no lleva | `200` la unidad. `404` si no existe |
| POST | `/api/catalog/units` | `code`, `type`, `description`, `nightlyRate` | `201` la unidad creada. `409` si el código está repetido. `400` si los datos son inválidos |
| PUT | `/api/catalog/units/{id}` | igual que el POST | `200` unidad actualizada. `409` si está inactiva o el código se repite. `404` si no existe |
| DELETE | `/api/catalog/units/{id}` | no lleva | `204`, desactiva la unidad (no la borra). `409` si tiene reservas activas. `404` si no existe |
| GET | `/api/catalog/available?from=&to=` | no lleva | `200` unidades activas y libres en ese período |
| GET | `/api/catalog/units/{id}/availability?from=&to=` | no lleva | `200` `{ "available": true }` o `{ "available": false }`. `404` si la unidad no existe |

Fechas `from` y `to` en formato `AAAA-MM-DD`. Si `to` no es posterior a `from`, responde `400`.

## Endpoints internos

Los usa `ms-andesstay-reservations`. No están pensados para clientes finales.

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/internal/allocations` | `reservationId`, `unitId`, `from`, `to` | `204` asignación registrada. `409` si la unidad está ocupada en esas fechas, está inactiva, o la reserva ya está asignada con otros datos. `404` si la unidad no existe. `400` si las fechas son inválidas |
| DELETE | `/internal/allocations/{reservationId}` | no lleva | `204` libera la unidad. Si la asignación no existe, también responde `204` |

`POST /internal/allocations` es idempotente: repetir la misma solicitud (misma reserva, unidad y fechas) responde `204` sin duplicar la asignación.

## Reglas de negocio

- Una unidad **no puede asignarse** a dos reservas cuyas fechas se superponen: la segunda recibe `409`.
- Una unidad **inactiva** no aparece como disponible y no acepta asignaciones.
- **Desactivar** una unidad (`DELETE`) falla con `409` si tiene asignaciones activas.
- Los códigos de unidad no se pueden repetir (`409`).
- Al liberar una asignación (`DELETE /internal/allocations/{id}`), la unidad vuelve a quedar disponible para esas fechas.

## Códigos de error

| Código | Cuándo |
|---|---|
| `400` | Datos inválidos o `to` no posterior a `from` |
| `401` | Falta el header `X-Internal-Token` o es incorrecto |
| `404` | La unidad no existe |
| `409` | Código repetido, unidad inactiva, unidad con reservas activas, o unidad ocupada en esas fechas |

El cuerpo de las respuestas de error incluye `status` y `error` (por ejemplo `Conflict`), pero no el mensaje detallado.
