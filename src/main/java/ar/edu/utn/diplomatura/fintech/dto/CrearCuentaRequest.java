package ar.edu.utn.diplomatura.fintech.dto;

import ar.edu.utn.diplomatura.fintech.entidades.TipoCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Datos para crear una cuenta")
public record CrearCuentaRequest(

        @Schema(description = "Tipo de cuenta", example = "CUENTA_CORRIENTE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El tipo es obligatorio (CAJA_AHORRO o CUENTA_CORRIENTE)")
        TipoCuenta tipo,

        @Schema(description = "Numero de cuenta, unico", example = "0001-0002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El numero de cuenta es obligatorio")
        String numero,

        @Schema(description = "Id del usuario titular (debe existir)", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El id del usuario titular es obligatorio")
        Long usuarioId,

        @Schema(description = "Saldo inicial. Si no se envia, la cuenta arranca en 0", example = "5000")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        BigDecimal saldoInicial,

        @Schema(description = "Descubierto permitido. Solo para CUENTA_CORRIENTE; si no se envia, es 0",
                example = "20000")
        @PositiveOrZero(message = "El descubierto no puede ser negativo")
        BigDecimal descubierto
) {
}
