package ar.edu.utn.diplomatura.fintech.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Cuenta corriente: se puede retirar mas que el saldo, hasta el descubierto.
 * - extends Cuenta: hereda atributos y operaciones.
 */
@Entity
@DiscriminatorValue("CUENTA_CORRIENTE")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class CuentaCorriente extends Cuenta {

    // Monto que puede quedar en negativo
    @Column(precision = 15, scale = 2)
    private BigDecimal descubierto = BigDecimal.ZERO;

    public CuentaCorriente(String numero, Usuario titular, BigDecimal descubierto) {
        super(numero, titular);
        this.descubierto = descubierto;
    }

    @Override
    public BigDecimal calcularLimiteRetiro() {
        return getSaldo().add(descubierto);
    }
}
