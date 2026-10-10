package ar.edu.utn.diplomatura.fintech.entidades;

import java.math.BigDecimal;

/**
 * Interfaz para las cuentas que generan intereses. Solo la implementa CajaDeAhorro.
 * Es interfaz y no un metodo en Cuenta porque no todas las cuentas generan intereses.
 */
public interface GeneraInteres {

    BigDecimal calcularInteresMensual();

    default void acreditarInteres() {
        BigDecimal interes = calcularInteresMensual();
        if (interes.compareTo(BigDecimal.ZERO) > 0) {
            depositarInteres(interes);
        }
    }

    void depositarInteres(BigDecimal interes);
}
