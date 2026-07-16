package com.talenthub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // TODO(auth): re-enable OAuth2 once Keycloak is available. With issuer-uri set,
        // Spring contacts Keycloak at startup, so the gateway cannot boot without it.
        // For local Docker we permit all traffic and skip the resource server.
        http.csrf(csrfSpec -> csrfSpec.disable())
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .oauth2ResourceServer(oAuth2ResourceServerSpec ->
                        oAuth2ResourceServerSpec.jwt((jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter()))));

        return http.build();
    }

    private ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        converter.setJwtGrantedAuthoritiesConverter((jwt) -> {
            return Flux.fromIterable(authoritiesConverter.convert(jwt));
        });

        return converter;
    }
}