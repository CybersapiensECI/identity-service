package co.edu.escuelaing.alphaeci.identity_service.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("AlphaECI Identity Service")
                        .version("1.0.0")
                        .description("""
                                Microservice responsible for authentication and identity management \
                                within the AlphaECI platform. Handles login, token issuance, \
                                token refresh, logout, OTP verification, and password management.

                                **Authentication:** Endpoints that require authentication expect a valid \
                                **JWT Bearer token** in the `Authorization` header. Tokens are issued \
                                by this service upon successful login or OTP validation.

                                **Token lifecycle:** Access tokens are short-lived (15 min). \
                                Use the refresh endpoint with a valid refresh token to obtain a new pair. \
                                Logout revokes both tokens immediately.

                                **Email verification:** After registration, users must verify their email \
                                via a one-time OTP before being allowed to log in.
                                """)
                        .contact(new Contact()
                                .name("AlphaECI Team — Escuela Colombiana de Ingeniería Julio Garavito")
                                .email("juanspider310@gmail.com")))
                .tags(Arrays.asList(
                        new Tag().name("Auth")
                                .description(
                                        "Core authentication endpoints: login with credentials, refresh an expired access token, and logout to revoke an active session."),
                        new Tag().name("OTP")
                                .description(
                                        "One-time password endpoints: validate an OTP code to complete email verification, or resend a new OTP when the previous one has expired or was not received."),
                        new Tag().name("Verification")
                                .description(
                                        "Email verification flow: initiate the verification process which triggers an OTP email to the registered address."),
                        new Tag().name("Password")
                                .description(
                                        "Password management: change the current password (authenticated), request a password reset link via email, or confirm a reset using a valid reset token.")));
    }
}
