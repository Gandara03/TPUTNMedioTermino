package ar.edu.utn.diplomatura.fintech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * JSON que llega en PUT /cuentas/{id}.
 *
 * Solo se puede cambiar el numero y el descubierto.
 * El SALDO no se edita por PUT a proposito: cambia unicamente con depositos
 * y retiros (si no, cualquiera podria "ponerse" plata).
 * El tipo y el titular tampoco cambian: seria otra cuenta.
 */
@Schema(description = "Datos para actualizar una cuenta. El saldo, el tipo y el titular no se modifican")
public record ActualizarCuentaRequest(

        @Schema(description = "Nuevo numero de cuenta, unico", example = "0001-0099",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El numero de cuenta es obligatorio")
        String numero,

        @Schema(description = "Nuevo descubierto. Solo valido para CUENTA_CORRIENTE", example = "50000")
        @PositiveOrZero(message = "El descubierto no puede ser negativo")
        BigDecimal descubierto
) {
}
