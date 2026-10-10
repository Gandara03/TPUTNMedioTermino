package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: la cuenta no existe o fue dada de baja -> 404.
 */
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long id) {
        super("No existe una cuenta con id " + id);
    }
}
