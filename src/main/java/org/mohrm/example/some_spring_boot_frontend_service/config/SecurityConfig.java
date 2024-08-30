package org.mohrm.example.some_spring_boot_frontend_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        /*
         * We only require that the caller of our services provides a valid JWT (bearer token) to
         * be authenticated.
         * This token is not passed any further, hence we do not need to configure oauth2Client.
         */
        http.authorizeExchange((exchange) -> {
            exchange.anyExchange().authenticated();
        }).oauth2ResourceServer(c -> c.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
