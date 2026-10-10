package ar.edu.utn.diplomatura.fintech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Datos para actualizar una cuenta. El saldo, el tipo y el titular no se modifican")
public record ActualizarCuentaRequest(

        @Schema(description = "Nuevo numero de cuenta, unico", example = "0001-0004",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El numero de cuenta es obligatorio")
        String numero,

        @Schema(description = "Nuevo descubierto. Solo valido para CUENTA_CORRIENTE", example = "60000")
        @PositiveOrZero(message = "El descubierto no puede ser negativo")
        BigDecimal descubierto
) {
}
