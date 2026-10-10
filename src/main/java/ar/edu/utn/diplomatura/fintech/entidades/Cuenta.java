package ar.edu.utn.diplomatura.fintech.entidades;

import ar.edu.utn.diplomatura.fintech.excepciones.SaldoInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Cuenta bancaria. Es ABSTRACTA: no se puede crear una "Cuenta" ,
 * solo una CajaDeAhorro o una CuentaCorriente (herencia).
 * Implementa la interfaz: cumple el contrato depositar / retirar / limite.
 **/
@Entity
@Table(name = "cuentas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cuenta")
@Data
@NoArgsConstructor
public abstract class Cuenta implements Operable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activa = true;

    /**
     * Muchas cuentas pertenecen a UN usuario (many to one).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario titular;

    protected Cuenta(String numero, Usuario titular) {
        this.numero = numero;
        this.titular = titular;
    }

    /**
     * Cada tipo de cuenta define cuanto se puede retirar .
     * Es abstracto: lo implementan CajaDeAhorro y CuentaCorriente.
     */
    @Override
    public abstract BigDecimal calcularLimiteRetiro();

    @Override
    public void depositar(BigDecimal monto) {
        validarMontoPositivo(monto);
        saldo = saldo.add(monto);
    }

    @Override
    public void retirar(BigDecimal monto) {
        validarMontoPositivo(monto);
        BigDecimal limite = calcularLimiteRetiro();   //  cada subclase lo calcula distinto
        if (monto.compareTo(limite) > 0) {
            throw new SaldoInsuficienteException(monto, limite);
        }
        saldo = saldo.subtract(monto);
    }

    private void validarMontoPositivo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }
}
