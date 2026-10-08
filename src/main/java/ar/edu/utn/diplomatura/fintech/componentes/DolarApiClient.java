package ar.edu.utn.diplomatura.fintech.componentes;

import ar.edu.utn.diplomatura.fintech.dto.CotizacionDolar;
import ar.edu.utn.diplomatura.fintech.excepciones.ServicioExternoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Cliente de la API EXTERNA DolarApi (https://dolarapi.com): cotizaciones del dolar en Argentina.
 *
 * @Component: Spring crea una unica instancia y la inyecta donde se necesite.
 *
 * RestClient (Spring 6.1+): cliente HTTP para consumir otras APIs REST.
 * Arma la request, la envia y convierte el JSON de respuesta en un objeto Java.
 */
@Component
public class DolarApiClient {

    private final RestClient restClient;

    /**
     * @Value toma la URL del application.yaml (dolarapi.url).
     * Si no esta definida, usa el valor que va despues de los dos puntos.
     */
    public DolarApiClient(@Value("${dolarapi.url:https://dolarapi.com/v1/dolares}") String baseUrl) {
        // Timeouts: si DolarApi tarda mas de 5 segundos, cortamos en vez de colgar nuestra API.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * GET https://dolarapi.com/v1/dolares/{casa}
     *
     * @param casa tipo de dolar: oficial, blue, bolsa, contadoconliqui, mayorista, cripto, tarjeta
     *
     * TRY/CATCH: llamar a otra API puede fallar por motivos ajenos a nosotros.
     * - HttpClientErrorException.NotFound (404): el tipo de dolar no existe -> error del cliente (400).
     * - RestClientException: cualquier otro problema (caida, timeout, sin internet) -> 503.
     * El orden importa: el catch mas especifico va primero, porque NotFound
     * tambien es una RestClientException.
     */
    public CotizacionDolar obtenerCotizacion(String casa) {
        try {
            CotizacionDolar cotizacion = restClient.get()
                    .uri("/{casa}", casa)
                    .retrieve()
                    .body(CotizacionDolar.class);

            if (cotizacion == null || cotizacion.venta() == null) {
                throw new ServicioExternoException("DolarApi respondio sin cotizacion para '" + casa + "'", null);
            }
            return cotizacion;

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(
                    "Tipo de dolar invalido: '" + casa + "'. Usar oficial, blue, bolsa, contadoconliqui, mayorista, cripto o tarjeta");
        } catch (RestClientException e) {
            throw new ServicioExternoException("No se pudo obtener la cotizacion del dolar. Intente mas tarde.", e);
        }
    }
}
