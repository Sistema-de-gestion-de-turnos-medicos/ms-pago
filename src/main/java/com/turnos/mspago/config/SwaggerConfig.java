package com.turnos.mspago.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración central de OpenAPI / Swagger UI para el microservicio de pagos.
 * <p>
 * Define la metadata general de la documentación (título, versión, contacto)
 * y el esquema de seguridad Bearer (JWT) que se utiliza en los endpoints
 * protegidos mediante {@code @PreAuthorize}.
 * <p>
 * La documentación queda disponible en:
 * <ul>
 *     <li>JSON: {@code /v3/api-docs}</li>
 *     <li>UI:   {@code /doc/swagger-ui.html}</li>
 * </ul>
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8089").description("Servidor local")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("MS Pago - API de Gestión de Pagos")
                .description("""
                        Microservicio encargado de la gestión de pagos dentro del
                        Sistema de Gestión de Turnos Médicos. Permite registrar,
                        consultar, aprobar, rechazar, cancelar y reembolsar pagos
                        asociados a turnos y pacientes.""")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Equipo Turnos Médicos")
                        .email("soporte@turnosmedicos.com"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el token JWT obtenido al autenticarse. Formato: Bearer {token}");
    }
}
