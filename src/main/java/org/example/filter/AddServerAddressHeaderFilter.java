package org.example.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 添加服务地址响应头
 *
 * @author: huojie
 * @date: 2024/01/09 16:12
 **/
@Component
public class AddServerAddressHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 获取转发的目的服务地址和端口号
            URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (uri != null) {
                String destinationAddress = uri.getHost() + ":" + (uri.getPort() < 0 ? "80" : uri.getPort());
                // 将服务地址和端口号添加到响应头中
                exchange.getResponse().getHeaders().add("X-Server-Address", destinationAddress);
            }
        }));
    }

    @Override
    public int getOrder() {
        // 设置过滤器的执行顺序
        return Ordered.LOWEST_PRECEDENCE;
    }
}
