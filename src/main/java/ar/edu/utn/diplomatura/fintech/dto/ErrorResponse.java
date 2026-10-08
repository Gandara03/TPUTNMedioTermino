package ar.edu.utn.diplomatura.fintech.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de todas las respuestas de error de la API.
 * Lo arma el ManejadorGlobalDeExcepciones.
 *
 * @JsonInclude(NON_NULL): si "campos" es null, no aparece en el JSON.
 */
@Schema(description = "Respuesta de error")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "Codigo HTTP", example = "404")
        int status,

        @Schema(description = "Descripcion del error", example = "No existe una cuenta con id 99")
        String error,

        @Schema(description = "Errores de validacion por campo (solo en 400 por datos invalidos)",
                example = "{\"numero\": \"El numero de cuenta es obligatorio\"}")
        Map<String, String> campos,

        @Schema(description = "Fecha y hora del error", example = "2026-10-08T10:15:30")
        String timestamp
) {

    public static ErrorResponse de(int status, String error, Map<String, String> campos) {
        return new ErrorResponse(status, error, campos, LocalDateTime.now().toString());
    }
}
