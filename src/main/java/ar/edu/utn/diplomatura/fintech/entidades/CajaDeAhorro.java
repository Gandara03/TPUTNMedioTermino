package ar.edu.utn.diplomatura.fintech.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Caja de ahorro: solo se puede retirar hasta el saldo disponible y genera intereses.
 *
 * - extends Cuenta: hereda atributos y operaciones (y con eso, la interfaz Operable).
 * - implements GeneraInteres: suma una capacidad que la cuenta corriente no tiene.
 *
 * @DiscriminatorValue: valor que se guarda en la columna tipo_cuenta para esta subclase.
 * No lleva @Table: hereda la tabla "cuentas" de Cuenta (SINGLE_TABLE).
 */
@Entity
@DiscriminatorValue("CAJA_AHORRO")
@NoArgsConstructor
public class CajaDeAhorro extends Cuenta implements GeneraInteres {

    /** Tasa nominal anual: 30% (0.30). static final = constante, no se guarda en la base. */
    public static final BigDecimal TASA_NOMINAL_ANUAL = new BigDecimal("0.30");

    public CajaDeAhorro(String numero, Usuario titular) {
        super(numero, titular);
    }

    @Override
    public BigDecimal calcularLimiteRetiro() {
        return getSaldo();
    }

    /** saldo x tasa anual / 12, redondeado a 2 decimales. */
    @Override
    public BigDecimal calcularInteresMensual() {
        return getSaldo()
                .multiply(TASA_NOMINAL_ANUAL)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_EVEN);
    }

    @Override
    public void depositarInteres(BigDecimal interes) {
        depositar(interes);
    }
}
