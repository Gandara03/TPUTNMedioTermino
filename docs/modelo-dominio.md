# Modelo de dominio

Un **Usuario** puede tener **varias Cuentas** (`@OneToMany`) y cada Cuenta pertenece a **un único** Usuario, su titular (`@ManyToOne`).
`Cuenta` es **abstracta**: no se instancia directamente, sino a través de `CajaDeAhorro` o `CuentaCorriente` (herencia).
`Cuenta` implementa la interfaz `Operable` (contrato de operaciones) y solo `CajaDeAhorro` implementa `GeneraInteres` (capacidad opcional).

## Diagrama de clases

```mermaid
classDiagram
    direction LR

    class Usuario {
        -Long id
        -String nombre
        -String apellido
        -String cuit
        -String email
        -List~Cuenta~ cuentas
        +agregarCuenta(Cuenta c) void
    }

    class Cuenta {
        <<abstract>>
        -Long id
        -String numero
        -BigDecimal saldo
        -Usuario titular
        +depositar(BigDecimal monto) void
        +retirar(BigDecimal monto) void
        +calcularLimiteRetiro()* BigDecimal
    }

    class CajaDeAhorro {
        +calcularLimiteRetiro() BigDecimal
    }

    class CuentaCorriente {
        -BigDecimal descubierto
        +calcularLimiteRetiro() BigDecimal
    }

    class Operable {
        <<interface>>
        +depositar(BigDecimal monto) void
        +retirar(BigDecimal monto) void
        +calcularLimiteRetiro() BigDecimal
    }

    class GeneraInteres {
        <<interface>>
        +calcularInteresMensual() BigDecimal
        +acreditarInteres() void
        +depositarInteres(BigDecimal interes) void
    }

    Operable <|.. Cuenta : implements
    GeneraInteres <|.. CajaDeAhorro : implements
    Usuario "1" --> "0..*" Cuenta : @OneToMany cuentas
    Cuenta "0..*" --> "1" Usuario : @ManyToOne titular
    Cuenta <|-- CajaDeAhorro : extends
    Cuenta <|-- CuentaCorriente : extends
```

## Cómo queda en la base (MySQL)

Herencia mapeada con `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`: todas las cuentas viven en una sola tabla `cuentas` y la columna `tipo_cuenta` indica de qué subclase es cada fila.

```mermaid
erDiagram
    USUARIOS ||--o{ CUENTAS : "titular"
    USUARIOS {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR cuit UK
        VARCHAR email UK
    }
    CUENTAS {
        BIGINT id PK
        VARCHAR tipo_cuenta "CAJA_AHORRO | CUENTA_CORRIENTE"
        VARCHAR numero UK
        DECIMAL saldo
        DECIMAL descubierto "solo CUENTA_CORRIENTE"
        BIGINT usuario_id FK
    }
```

| Regla de negocio | Caja de ahorro | Cuenta corriente |
| --- | --- | --- |
| Límite de retiro (`calcularLimiteRetiro()`) | El saldo | Saldo + descubierto |
| Puede quedar en negativo | No | Sí, hasta el descubierto |
