package ar.edu.utn.diplomatura.fintech.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Caja de ahorro: solo se puede retirar hasta el saldo disponible y genera intereses
 * - extends Cuenta: hereda atributos y operaciones (y con eso, la interfaz Operable)
 * - Interfaces GeneraInteres: suma una capacidad que la cuenta corriente no tiene
 */
@Entity
@DiscriminatorValue("CAJA_AHORRO")
@NoArgsConstructor
public class CajaDeAhorro extends Cuenta implements GeneraInteres {

    /** Tasa nominal anual: 20% (0.20). static final = constante */
    public static final BigDecimal TASA_NOMINAL_ANUAL = new BigDecimal("0.20");

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
