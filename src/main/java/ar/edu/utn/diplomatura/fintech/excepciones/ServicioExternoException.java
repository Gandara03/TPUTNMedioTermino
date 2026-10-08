package ar.edu.utn.diplomatura.fintech.excepciones;

/**
 * Excepcion custom: fallo una API externa (DolarApi caida, timeout, sin internet...).
 * No es culpa del cliente ni de nuestra API, por eso el ManejadorGlobalDeExcepciones
 * responde 503 Service Unavailable ("probá mas tarde").
 */
public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
