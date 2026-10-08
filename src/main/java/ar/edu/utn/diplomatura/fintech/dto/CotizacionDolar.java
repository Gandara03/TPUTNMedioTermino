package ar.edu.utn.diplomatura.fintech.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * JSON que devuelve la API EXTERNA DolarApi (https://dolarapi.com).
 *
 * Ejemplo de GET https://dolarapi.com/v1/dolares/oficial:
 * {
 *   "moneda": "USD", "casa": "oficial", "nombre": "Oficial",
 *   "compra": 1490, "venta": 1540,
 *   "fechaActualizacion": "2026-10-07T16:00:00.000Z"
 * }
 *
 * Los nombres de los campos del record coinciden con los del JSON,
 * asi Jackson los completa solo.
 * @JsonIgnoreProperties(ignoreUnknown = true): si DolarApi agrega campos nuevos,
 * los ignoramos en vez de fallar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotizacionDolar(
        String moneda,
        String casa,
        String nombre,
        BigDecimal compra,
        BigDecimal venta,
        String fechaActualizacion
) {
}
