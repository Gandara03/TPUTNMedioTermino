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
 * Cuenta corriente: permite retirar mas que el saldo, hasta el descubierto acordado.
 *
 * callSuper = true: equals, hashCode y toString incluyen tambien los atributos de Cuenta.
 */
@Entity
@DiscriminatorValue("CUENTA_CORRIENTE")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class CuentaCorriente extends Cuenta {

    // Monto que puede quedar en negativo. Columna que solo usan las cuentas corrientes.
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
