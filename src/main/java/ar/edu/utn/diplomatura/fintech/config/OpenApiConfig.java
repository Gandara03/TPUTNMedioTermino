package ar.edu.utn.diplomatura.fintech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion general de la documentacion Swagger / OpenAPI.
 *
 * @Configuration: clase de configuracion de Spring.
 * @Bean: el objeto que devuelve el metodo queda registrado en el contenedor;
 *   springdoc lo usa como encabezado de la documentacion.
 *
 * UI:   http://localhost:8080/swagger-ui.html
 * JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiFintechOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Fintech")
                        .version("1.0.0")
                        .description("""
                                API REST que simula un sistema fintech basico: gestion de usuarios \
                                y de sus cuentas (caja de ahorro y cuenta corriente).

                                Trabajo practico de medio termino - Diplomatura en Desarrollo de Software \
                                FinTech: IA y Microservicios (UTN / Codeki).

                                **Errores:** todas las respuestas de error tienen el mismo formato \
                                (`status`, `error`, `campos`, `timestamp`)."""))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .tags(List.of(
                        new Tag().name("Usuarios").description("Alta y consulta de usuarios (titulares de cuentas)"),
                        new Tag().name("Cuentas").description("ABM de cuentas: caja de ahorro y cuenta corriente")));
    }
}
