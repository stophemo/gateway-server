package org.example.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * 添加请求方ip到请求头,目的地址到响应头
 *
 * @author: huojie
 * @date: 2024/01/25 10:30
 **/
@Slf4j
@Component
public class AddressToHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 目标地址
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String destinationAddress = (uri != null) ? uri.getHost() + ":" + (uri.getPort() < 0 ? "80" : uri.getPort()) : "";
        // 请求方ip
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        String clientIP = (remoteAddress != null) ? remoteAddress.getAddress().getHostAddress() : "";

        log.info("Client IP: {}", clientIP);
        log.info("Destination Address: {}", destinationAddress);

        // 修改请求头、响应头
        ServerHttpRequest request = exchange.getRequest().mutate().header("X-Client-IP", clientIP).build();
        ServerWebExchange newExchange = exchange.mutate().request(request).build();
        newExchange.getResponse().getHeaders().add("X-Server-Address", destinationAddress);
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}