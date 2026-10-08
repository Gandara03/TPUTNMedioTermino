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
 * Cuenta bancaria. Es ABSTRACTA: no se puede crear una "Cuenta" a secas,
 * solo una CajaDeAhorro o una CuentaCorriente (herencia).
 * Implementa la interfaz Operable: cumple el contrato depositar / retirar / limite.
 *
 * @Inheritance(SINGLE_TABLE): todas las subclases se guardan en UNA sola tabla "cuentas".
 * @DiscriminatorColumn: columna que indica de que subclase es cada fila
 *   (CAJA_AHORRO o CUENTA_CORRIENTE, definido con @DiscriminatorValue en cada hija).
 */
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

    /**
     * BigDecimal y no double: con double las cuentas de plata tienen errores
     * de redondeo (0.1 + 0.2 = 0.30000000000000004).
     * precision = 15, scale = 2: hasta 15 digitos, 2 de ellos decimales -> DECIMAL(15,2).
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    /**
     * Baja logica: en vez de borrar la fila, se marca como inactiva.
     * columnDefinition "default true": si la tabla ya tenia cuentas, al crear esta
     * columna quedan activas (sin el default, MySQL las pondria en false).
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activa = true;

    /**
     * Muchas cuentas pertenecen a UN usuario (muchos a uno).
     * Este es el lado "dueño" de la relacion: guarda la columna usuario_id (clave foranea).
     * optional = false: toda cuenta tiene que tener titular.
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
     * Cada tipo de cuenta define cuanto se puede retirar (polimorfismo).
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
        // compareTo devuelve > 0 si monto es mayor que el limite.
        BigDecimal limite = calcularLimiteRetiro();   // polimorfismo: cada subclase lo calcula distinto
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
