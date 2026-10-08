package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: se lanza cuando se busca un usuario por id y no existe.
 *
 * Extiende RuntimeException (unchecked): no obliga a poner try/catch en cada
 * llamada; la atrapa el ManejadorGlobalDeExcepciones y responde 404.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe un usuario con id " + id);
    }
}
