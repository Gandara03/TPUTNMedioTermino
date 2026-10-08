package ar.edu.utn.diplomatura.fintech.controladores;

import ar.edu.utn.diplomatura.fintech.dto.ActualizarCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.CrearCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.CuentaResponse;
import ar.edu.utn.diplomatura.fintech.dto.ErrorResponse;
import ar.edu.utn.diplomatura.fintech.dto.SaldoUsdResponse;
import ar.edu.utn.diplomatura.fintech.servicios.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de cuentas. Recibe y devuelve DTOs, nunca la entidad directamente.
 * Las anotaciones @Operation / @ApiResponse documentan cada endpoint en Swagger.
 */
@Tag(name = "Cuentas")
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    /** stream().map(...): convierte cada entidad Cuenta en un CuentaResponse (Clase 12). */
    @Operation(summary = "Listar cuentas",
            description = "Devuelve todas las cuentas activas. Las dadas de baja logica no se incluyen.")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas (puede estar vacia)")
    @GetMapping
    public List<CuentaResponse> listar() {
        return cuentaService.listarTodas().stream()
                .map(CuentaResponse::desde)
                .toList();
    }

    @Operation(summary = "Buscar cuenta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe o fue dada de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public CuentaResponse buscarPorId(@Parameter(description = "Id de la cuenta", example = "1") @PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.buscarPorId(id));
    }

    /**
     * GET /cuentas/1/saldo-usd?casa=blue
     * Consume la API externa DolarApi para convertir el saldo a dolares.
     */
    @Operation(summary = "Saldo en dolares (API externa)",
            description = "Convierte el saldo de la cuenta a USD con la cotizacion de venta obtenida de "
                    + "DolarApi (https://dolarapi.com).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo convertido"),
            @ApiResponse(responseCode = "400", description = "Tipo de dolar invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La cuenta no existe o fue dada de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "DolarApi no respondio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/saldo-usd")
    public SaldoUsdResponse saldoEnDolares(
            @Parameter(description = "Id de la cuenta", example = "1") @PathVariable Long id,
            @Parameter(description = "Tipo de dolar",
                    schema = @Schema(allowableValues = {"oficial", "blue", "bolsa", "contadoconliqui",
                            "mayorista", "cripto", "tarjeta"}))
            @RequestParam(defaultValue = "oficial") String casa) {
        return cuentaService.consultarSaldoEnDolares(id, casa);
    }

    /** @Valid: chequea las anotaciones del DTO antes de entrar. Si falla, responde 400. */
    @Operation(summary = "Crear cuenta",
            description = "Crea una CAJA_AHORRO o una CUENTA_CORRIENTE para un usuario existente. "
                    + "El descubierto solo aplica a cuentas corrientes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una cuenta con ese numero",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario titular no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crear(@Valid @RequestBody CrearCuentaRequest request) {
        return CuentaResponse.desde(cuentaService.crear(request));
    }

    @Operation(summary = "Actualizar cuenta",
            description = "Modifica el numero y/o el descubierto. El saldo NO se edita por PUT: "
                    + "solo cambia con depositos y retiros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada"),
            @ApiResponse(responseCode = "400",
                    description = "Datos invalidos o descubierto en una caja de ahorro",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe otra cuenta con ese numero",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe o fue dada de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public CuentaResponse actualizar(@Parameter(description = "Id de la cuenta", example = "2") @PathVariable Long id,
                                     @Valid @RequestBody ActualizarCuentaRequest request) {
        return CuentaResponse.desde(cuentaService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar cuenta",
            description = "Baja logica (por defecto: queda en la base como inactiva) o fisica (se borra). "
                    + "Solo se puede eliminar una cuenta con saldo 0.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada"),
            @ApiResponse(responseCode = "400", description = "La cuenta tiene saldo o el modo es invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe o ya fue dada de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Id de la cuenta", example = "3") @PathVariable Long id,
            @Parameter(description = "Tipo de baja: logico o fisico", schema = @Schema(allowableValues = {"logico", "fisico"}))
            @RequestParam(defaultValue = "logico") String modo) {
        cuentaService.eliminar(id, modo);
        return ResponseEntity.noContent().build();   // 204
    }
}
