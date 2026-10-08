package ar.edu.utn.diplomatura.fintech.entidades;

import java.math.BigDecimal;

/**
 * Capacidad OPCIONAL: solo la implementan las cuentas que generan intereses
 * (CajaDeAhorro). CuentaCorriente no la implementa.
 *
 * Por eso es una interfaz y no un metodo en Cuenta: no todas las cuentas
 * generan intereses, y una clase puede sumar interfaces sin cambiar su herencia.
 */
public interface GeneraInteres {

    /** Interes que corresponde al mes, segun el saldo actual. */
    BigDecimal calcularInteresMensual();

    /**
     * Suma el interes del mes al saldo.
     * Metodo "default" (Java 8+): la interfaz trae una implementacion lista
     * que las clases pueden usar tal cual o sobrescribir.
     */
    default void acreditarInteres() {
        BigDecimal interes = calcularInteresMensual();
        if (interes.compareTo(BigDecimal.ZERO) > 0) {
            depositarInteres(interes);
        }
    }

    /** Como se deposita el interes lo decide la clase que implementa. */
    void depositarInteres(BigDecimal interes);
}
