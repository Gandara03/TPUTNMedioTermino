package ar.edu.utn.diplomatura.fintech.excepciones;

import java.math.BigDecimal;

/**
 * Excepcion custom: se quiere retirar mas que el limite de la cuenta -> 400.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal monto, BigDecimal limite) {
        super("Saldo insuficiente: se intento retirar " + monto + " y el limite es " + limite);
    }
}
