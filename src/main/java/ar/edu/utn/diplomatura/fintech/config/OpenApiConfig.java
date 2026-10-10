package ar.edu.utn.diplomatura.fintech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion  Swagger / OpenAPI.
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

                                Trabajo practico de medio termino"""))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .tags(List.of(
                        new Tag().name("Usuarios").description("Alta y consulta de usuarios (titulares de cuentas)"),
                        new Tag().name("Cuentas").description("ABM de cuentas: caja de ahorro y cuenta corriente")));
    }
}
