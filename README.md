# API Fintech

TP de medio término de la Diplomatura en Desarrollo de Software FinTech (UTN / Codeki).

Es una API REST hecha con Spring Boot que maneja usuarios y sus cuentas bancarias. Cada usuario puede tener varias cuentas, que pueden ser caja de ahorro o cuenta corriente. Los datos se guardan en MySQL este se configura en el archivo YAML

2. Configurá la contraseña de MySQL. La conexión está en `src/main/resources/application.yaml`
   La base `fintech` y las tablas se crean solas al arrancar.

La API queda en `http://localhost:8080`.

La primera vez que arranca FintechApplication.java, si la base está vacía, se cargan datos de ejemplo (clase `CargaDatosIniciales`): 3 usuarios  con algunas cuentas, así se puede probar sin crear nada a mano.

La documentación de Swagger está en `http://localhost:8080/swagger-ui.html`. Desde ahí se pueden probar todos los endpoints y con la coleccion de postman

## Endpoints de cuentas

### GET /cuentas

Devuelve todas las cuentas activas. Se puede filtrar por usuario con el query param `usuarioId`.

```
GET http://localhost:8080/cuentas
GET http://localhost:8080/cuentas?usuarioId=1
```

Si el `usuarioId` no existe devuelve `404`.

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
    "titular": "Alexis Gandara"
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

- `POST /usuarios`: crea un usuario (`nombre`, `apellido`, `cuit`, `email`). Todos son obligatorios, el CUIT/CUIL va sin guiones (11 dígitos) y el email tiene que ser válido (si no, `400`). El CUIT/CUIL y el email no se pueden repetir (`409`).
- `GET /usuarios`: lista los usuarios.
- `GET /usuarios/{id}`: busca un usuario por id.

## Entidades y relaciones

**Usuario**: es el cliente. Tiene nombre, apellido, CUIT/CUIL y email. Se identifica por el CUIT/CUIL, que es único (igual que el email).

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

## Cómo está armado

El código está separado en capas: controladores, servicios y repositorios.

- Para crear y devolver cuentas se usan DTOs, porque `Cuenta` es abstracta y no se puede armar directo desde el JSON.
- Los errores se manejan en `ManejadorGlobalDeExcepciones`, con excepciones propias como `CuentaNoEncontradaException`. También hay `try/catch` en los servicios para los datos repetidos (CUIT/CUIL, email o número de cuenta).

## Tests

Hay tests unitarios con JUnit y Mockito para la lógica de las cuentas y para los servicios. Se corren con:

```
mvnw.cmd test
```

No necesitan MySQL porque usan una base H2 en memoria.

## Colección de Postman

En la carpeta `postman` está el archivo `postman_collection.json` con todos los endpoints listos para probar.

