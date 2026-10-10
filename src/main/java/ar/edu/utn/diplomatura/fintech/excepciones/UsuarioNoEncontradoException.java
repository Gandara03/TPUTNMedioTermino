package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: el usuario no existe -> 404.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe un usuario con id " + id);
    }
}
