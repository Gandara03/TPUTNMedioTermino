package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: se lanza cuando se busca una cuenta por id y no existe.
 * La atrapa el ManejadorGlobalDeExcepciones y responde 404.
 */
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long id) {
        super("No existe una cuenta con id " + id);
    }
}
