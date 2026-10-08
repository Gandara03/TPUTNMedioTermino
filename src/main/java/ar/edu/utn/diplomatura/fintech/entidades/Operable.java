package ar.edu.utn.diplomatura.fintech.entidades;

import java.math.BigDecimal;

/**
 * Contrato de las operaciones basicas de una cuenta: define QUE se puede hacer,
 * no COMO. Una interfaz no tiene estado (atributos); solo firmas de metodos.
 *
 * Interfaz vs clase abstracta:
 * - Operable (interfaz) dice "toda cuenta se puede depositar, retirar y tiene un limite".
 * - Cuenta (clase abstracta) implementa este contrato: guarda el saldo, resuelve
 *   depositar() y retirar() una sola vez para todas, y deja calcularLimiteRetiro()
 *   a cada subclase.
 * Una clase puede implementar VARIAS interfaces, pero extender UNA sola clase.
 */
public interface Operable {

    void depositar(BigDecimal monto);

    void retirar(BigDecimal monto);

    BigDecimal calcularLimiteRetiro();
}
