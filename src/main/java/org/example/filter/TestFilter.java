package org.example.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @author CYVATION-LXL
 */
@Service
public class TestFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI originalUri = request.getURI();
        try {
            URI mutatedUri = new URI(originalUri.getScheme(),
                    originalUri.getUserInfo(),
                    "192.168.9.202",
                    8100,
                    originalUri.getPath(),
                    originalUri.getQuery(),
                    originalUri.getFragment());
            request = request.mutate().uri(mutatedUri)
                    .header("GW-CLIENT-IP","").build();
        } catch (Exception e) {
            return Mono.error(new IllegalStateException("外 https 转内 http 错误", e));
        }
        return chain.filter(exchange.mutate().request(request).build());

//        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}