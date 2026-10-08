package ar.edu.utn.diplomatura.fintech.excepciones;

import java.math.BigDecimal;

/**
 * Excepcion custom: se intenta retirar mas de lo que permite la cuenta
 * (el saldo en una caja de ahorro, saldo + descubierto en una cuenta corriente).
 * El ManejadorGlobalDeExcepciones responde 400.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal monto, BigDecimal limite) {
        super("Saldo insuficiente: se intento retirar " + monto + " y el limite es " + limite);
    }
}
