package ar.edu.utn.diplomatura.fintech.entidades;

/**
 * Tipos de cuenta que se pueden crear.
 * Coinciden con los @DiscriminatorValue de cada subclase de Cuenta.
 */
public enum TipoCuenta {
    CAJA_AHORRO,
    CUENTA_CORRIENTE
}
