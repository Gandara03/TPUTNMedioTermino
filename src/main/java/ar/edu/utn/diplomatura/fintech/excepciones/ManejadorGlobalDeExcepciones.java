package ar.edu.utn.diplomatura.fintech.excepciones;

import ar.edu.utn.diplomatura.fintech.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atrapa las excepciones que salen de CUALQUIER controlador y las convierte
 * en una respuesta HTTP prolija (ErrorResponse), en vez de un error 500 con el stacktrace.
 *
 * @RestControllerAdvice: aplica a todos los @RestController.
 * @ExceptionHandler(X.class): este metodo se ejecuta cuando se lanza una X.
 */
@RestControllerAdvice
public class ManejadorGlobalDeExcepciones {

    /** 404: usuario o cuenta inexistente. */
    @ExceptionHandler({UsuarioNoEncontradoException.class, CuentaNoEncontradaException.class})
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** 409: dato unico repetido (numero de cuenta, DNI, email). */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(RecursoDuplicadoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** 503: fallo una API externa (DolarApi). El problema no es del cliente: que reintente despues. */
    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> servicioExterno(ServicioExternoException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    /** 400: se quiso retirar mas de lo permitido. */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> saldoInsuficiente(SaldoInsuficienteException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** 400: regla de negocio violada (ej: cuenta con saldo, monto invalido). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(IllegalArgumentException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** 400: fallo alguna validacion del DTO (@NotNull, @NotBlank, ...) marcada con @Valid. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Datos invalidos", campos);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, Map<String, String> campos) {
        return ResponseEntity.status(status).body(ErrorResponse.de(status.value(), mensaje, campos));
    }
}
