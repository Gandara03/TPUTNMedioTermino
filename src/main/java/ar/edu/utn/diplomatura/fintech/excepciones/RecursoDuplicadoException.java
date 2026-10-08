package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: se intenta crear o modificar algo con un dato que debe ser
 * unico y ya existe (numero de cuenta, DNI o email de usuario).
 * El ManejadorGlobalDeExcepciones responde 409 Conflict.
 *
 * Tiene un constructor que recibe la causa original (la excepcion de la base),
 * asi no se pierde la informacion de que fue lo que fallo.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public RecursoDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
