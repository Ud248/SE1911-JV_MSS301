package com.talenthub.gateway.filter;

import com.talenthub.gateway.utils.GatewayConstraints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Get jwt and extract user info

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap((jwtAuthenticationToken -> {
                    Jwt jwt = jwtAuthenticationToken.getToken();
                    String email = jwt.getClaimAsString("email");
                    String name = jwt.getClaimAsString("name");
                    String userId = jwt.getSubject();
                    String username = jwt.getClaimAsString("preferred_username");

                    List<String> roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");

                    String rolesAsString = String.join(",", roles);

                    log.info("Role as String={}", rolesAsString);

                    // Enrich header
                    ServerHttpRequest muteRequest = exchange.getRequest().mutate().headers((httpHeaders) -> {
                        httpHeaders.set(GatewayConstraints.HEADER_USER_ID, userId);
                        httpHeaders.set(GatewayConstraints.HEADER_EMAIL, email);
                        httpHeaders.set(GatewayConstraints.HEADER_ROLE_NAME, rolesAsString);

                    }).build();

                    return chain.filter(exchange.mutate().request(muteRequest).build());

                }))
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return GatewayConstraints.ORDER_JWT_AUTH;
    }
}
