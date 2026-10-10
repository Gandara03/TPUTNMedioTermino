package ar.edu.utn.diplomatura.fintech.entidades;

import java.math.BigDecimal;

/**
 * Interfaz con las operaciones de toda cuenta: depositar, retirar y limite de retiro.
 * Define QUE se puede hacer; la implementa Cuenta.
 */
public interface Operable {

    void depositar(BigDecimal monto);

    void retirar(BigDecimal monto);

    BigDecimal calcularLimiteRetiro();
}
