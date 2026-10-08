package ar.edu.utn.diplomatura.fintech.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Respuesta de GET /cuentas/{id}/saldo-usd: el saldo de la cuenta convertido a dolares.
 */
@Schema(description = "Saldo de la cuenta convertido a dolares segun la cotizacion de DolarApi")
public record SaldoUsdResponse(

        @Schema(description = "Id de la cuenta", example = "1")
        Long cuentaId,

        @Schema(description = "Numero de cuenta", example = "0001-0001")
        String numero,

        @Schema(description = "Saldo en pesos", example = "154000.00")
        BigDecimal saldoArs,

        @Schema(description = "Tipo de dolar usado", example = "oficial")
        String casa,

        @Schema(description = "Cotizacion de venta usada para convertir (ARS por USD)", example = "1540")
        BigDecimal cotizacionVenta,

        @Schema(description = "Saldo en dolares (saldoArs / cotizacionVenta)", example = "100.00")
        BigDecimal saldoUsd,

        @Schema(description = "Fecha de la cotizacion informada por DolarApi", example = "2026-10-07T16:00:00.000Z")
        String fechaCotizacion
) {
}
