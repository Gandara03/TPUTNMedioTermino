package ar.edu.utn.diplomatura.fintech.dto;

import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import ar.edu.utn.diplomatura.fintech.entidades.CuentaCorriente;
import ar.edu.utn.diplomatura.fintech.entidades.TipoCuenta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;


@Schema(description = "Cuenta devuelta por la API")
public record CuentaResponse(

        @Schema(description = "Id generado por la base", example = "2")
        Long id,

        @Schema(description = "Tipo de cuenta", example = "CUENTA_CORRIENTE")
        TipoCuenta tipo,

        @Schema(description = "Numero de cuenta", example = "0001-0002")
        String numero,

        @Schema(description = "Saldo actual", example = "5000.00")
        BigDecimal saldo,

        @Schema(description = "Descubierto permitido (null en cajas de ahorro)", example = "20000.00", nullable = true)
        BigDecimal descubierto,

        @Schema(description = "Cuanto se puede retirar: saldo (caja de ahorro) o saldo + descubierto (cuenta corriente)",
                example = "25000.00")
        BigDecimal limiteRetiro,

        @Schema(description = "Id del usuario titular", example = "1")
        Long usuarioId,

        @Schema(description = "Nombre y apellido del titular", example = "Ana Gomez")
        String titular
) {


    public static CuentaResponse desde(Cuenta cuenta) {
        TipoCuenta tipo = (cuenta instanceof CuentaCorriente) ? TipoCuenta.CUENTA_CORRIENTE : TipoCuenta.CAJA_AHORRO;
        BigDecimal descubierto = (cuenta instanceof CuentaCorriente cc) ? cc.getDescubierto() : null;

        return new CuentaResponse(
                cuenta.getId(),
                tipo,
                cuenta.getNumero(),
                cuenta.getSaldo(),
                descubierto,
                cuenta.calcularLimiteRetiro(),   // cada subclase lo calcula a su manera
                cuenta.getTitular().getId(),
                cuenta.getTitular().getNombre() + " " + cuenta.getTitular().getApellido()
        );
    }
}
