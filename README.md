# API Fintech

TP de medio término de la Diplomatura en Desarrollo de Software FinTech (UTN / Codeki).

Es una API REST hecha con Spring Boot que maneja usuarios y sus cuentas bancarias. Cada usuario puede tener varias cuentas, que pueden ser caja de ahorro o cuenta corriente. Los datos se guardan en MySQL.

## Cómo correrlo

Necesitás Java 21 (o más nuevo) y MySQL corriendo en el puerto 3306.

1. Cloná el repo y abrí la carpeta `api-fintech`.
2. Configurá la contraseña de MySQL. La conexión está en `src/main/resources/application.yaml`: el usuario es `root` y la contraseña no está escrita en el archivo, se toma de la variable de entorno `DB_PASSWORD` (si no está definida usa `root`).

   En Windows, desde una terminal:

   ```
   setx DB_PASSWORD "tu_clave"
   ```

   Después hay que cerrar y volver a abrir la terminal o el IDE para que la tome. En Linux/Mac es `export DB_PASSWORD=tu_clave`.

   La base `fintech` y las tablas se crean solas al arrancar.
3. Levantá la app con `mvnw.cmd spring-boot:run` (o `./mvnw spring-boot:run` en Linux/Mac), o ejecutando `FintechApplication` desde el IDE.

La API queda en `http://localhost:8080`.

La documentación de Swagger está en `http://localhost:8080/swagger-ui.html`. Desde ahí se pueden probar todos los endpoints.

## Endpoints de cuentas

### GET /cuentas

Devuelve todas las cuentas activas.

```
GET http://localhost:8080/cuentas
```

Respuesta:

```json
[
  {
    "id": 1,
    "tipo": "CAJA_AHORRO",
    "numero": "0001-0001",
    "saldo": 10000.00,
    "descubierto": null,
    "limiteRetiro": 10000.00,
    "usuarioId": 1,
    "titular": "Ana Gomez"
  }
]
```

`limiteRetiro` es lo máximo que se puede sacar de la cuenta: en la caja de ahorro es el saldo y en la cuenta corriente es el saldo más el descubierto.

### POST /cuentas

Crea una cuenta para un usuario que ya existe.

```
POST http://localhost:8080/cuentas
Content-Type: application/json

{
  "tipo": "CUENTA_CORRIENTE",
  "numero": "0001-0002",
  "usuarioId": 1,
  "saldoInicial": 5000,
  "descubierto": 20000
}
```

- `tipo`, `numero` y `usuarioId` son obligatorios.
- `tipo` puede ser `CAJA_AHORRO` o `CUENTA_CORRIENTE`.
- `saldoInicial` y `descubierto` son opcionales (por defecto 0). El descubierto solo se usa en cuentas corrientes.

Devuelve `201` con la cuenta creada. Si falta algún dato devuelve `400`, si el usuario no existe `404` y si el número de cuenta ya está usado `409`.

### GET /cuentas/{id}

Busca una cuenta por su id.

```
GET http://localhost:8080/cuentas/1
```

Devuelve `200` con la cuenta, o `404` si no existe:

```json
{
  "status": 404,
  "error": "No existe una cuenta con id 1",
  "timestamp": "2026-10-07T23:34:21.199"
}
```

### PUT /cuentas/{id} (bonus)

Permite cambiar el número de cuenta y el descubierto. El saldo no se puede modificar por acá, solo cambia con depósitos y retiros.

```
PUT http://localhost:8080/cuentas/2
Content-Type: application/json

{
  "numero": "0001-0099",
  "descubierto": 50000
}
```

Devuelve `200` con la cuenta actualizada. Si se manda descubierto para una caja de ahorro devuelve `400`.

### DELETE /cuentas/{id} (bonus)

Da de baja una cuenta. Por defecto hace una baja lógica (la cuenta queda en la base pero marcada como inactiva). Con `?modo=fisico` la borra de la tabla.

```
DELETE http://localhost:8080/cuentas/3
DELETE http://localhost:8080/cuentas/3?modo=fisico
```

Solo se pueden eliminar cuentas con saldo 0. Devuelve `204` si salió bien, `400` si la cuenta tiene saldo y `404` si no existe.

## Otros endpoints

Para poder crear cuentas hace falta un usuario, así que también están:

- `POST /usuarios`: crea un usuario (`nombre`, `apellido`, `dni`, `email`). El DNI y el email no se pueden repetir.
- `GET /usuarios`: lista los usuarios.
- `GET /usuarios/{id}`: busca un usuario por id.

Y un endpoint que consume una API externa:

- `GET /cuentas/{id}/saldo-usd?casa=oficial`: convierte el saldo de la cuenta a dólares usando la cotización de [DolarApi](https://dolarapi.com). `casa` puede ser `oficial`, `blue`, `bolsa`, etc. Si DolarApi no responde devuelve `503`.

## Entidades y relaciones

**Usuario**: es el cliente. Tiene nombre, apellido, DNI y email (el DNI y el email son únicos).

**Cuenta**: tiene número, saldo y un titular. Es una clase **abstracta** porque en la realidad no existe una "cuenta" a secas: siempre es de algún tipo. Por eso no se puede crear una `Cuenta` directamente, solo alguna de sus hijas:

- **CajaDeAhorro**: solo se puede retirar hasta el saldo. Además genera intereses.
- **CuentaCorriente**: tiene un descubierto, así que se puede retirar más que el saldo y quedar en negativo hasta ese límite.

Lo que tienen en común (depositar, retirar, el saldo) está resuelto una sola vez en `Cuenta`. Lo que cambia es cuánto se puede retirar, por eso `calcularLimiteRetiro()` es un método abstracto y cada hija lo implementa a su manera.

También hay dos interfaces:

- **Operable**: define las operaciones de toda cuenta (depositar, retirar, límite de retiro). La implementa `Cuenta`.
- **GeneraInteres**: solo la implementa `CajaDeAhorro`, porque no todas las cuentas generan intereses.

**Relación**: un usuario puede tener varias cuentas y cada cuenta pertenece a un solo usuario. En el código es un `@OneToMany` en `Usuario` y un `@ManyToOne` en `Cuenta`. En la base, la tabla `cuentas` tiene la columna `usuario_id`.

Las dos clases hijas se guardan en la misma tabla `cuentas` (herencia `SINGLE_TABLE`), con una columna `tipo_cuenta` que indica si es caja de ahorro o cuenta corriente.

```
Usuario 1 ──── * Cuenta (abstracta)
                    ├── CajaDeAhorro
                    └── CuentaCorriente
```

El diagrama completo está en `docs/modelo-dominio.md`.

## Cómo está armado

El código está separado en capas: controladores, servicios y repositorios.

- Para crear y devolver cuentas se usan DTOs, porque `Cuenta` es abstracta y no se puede armar directo desde el JSON.
- Los errores se manejan en `ManejadorGlobalDeExcepciones`, con excepciones propias como `CuentaNoEncontradaException`. También hay `try/catch` en los servicios para los datos repetidos y en el cliente de DolarApi.

## Tests

Hay tests unitarios con JUnit y Mockito para la lógica de las cuentas y para los servicios. Se corren con:

```
mvnw.cmd test
```

No necesitan MySQL porque usan una base H2 en memoria.

## Colección de Postman

En la carpeta `postman` está el archivo `API-Fintech.postman_collection.json` con todos los endpoints listos para probar.

Para usarla:

1. En Postman, ir a **Import** y elegir ese archivo.
2. Con la app corriendo, hacer clic derecho sobre la colección → **Run collection** → **Run**.

Los requests están ordenados en tres carpetas (Usuarios, Cuentas y Errores) y conviene correrlos en ese orden, porque los ids que se van creando se guardan en variables y los usan los requests siguientes. Cada uno tiene tests que verifican el código de respuesta.

Si la API corre en otro puerto, se cambia en la variable `base_url` de la colección.
