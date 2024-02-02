package com.tfswx.gateway.filter;

import com.tfswx.gateway.service.LocalRouteService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.tfswx.gateway.dto.TargetAddressGetInputDTO;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;

/**
 * 自定义过滤器
 *
 * @author: huojie
 * @date: 2024/01/25 10:30
 **/
@Slf4j
@Component
public class CustomFilter implements GlobalFilter, Ordered {

    @Resource
    private LocalRouteService localRouteService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.保存请求方ip、目的地址到头信息
        // 2.将请求转发到目的地址
        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String host = requestUri.getHost();
        String path = requestUri.getPath();

        String[] parts = host.split("\\.");
        String engineeringName = parts[3];

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String sourceIp;
        if (remoteAddress != null) {
            sourceIp = remoteAddress.getAddress().getHostAddress();
        } else {
            sourceIp = null;
        }

        TargetAddressGetInputDTO inputDTO = new TargetAddressGetInputDTO(engineeringName, sourceIp, path);
        String targetAddress = localRouteService.getTargetAddress(inputDTO);
        log.info("Target address: {}", targetAddress);

        URI newUri = UriComponentsBuilder.fromHttpUrl(targetAddress + path).build().toUri();
        log.info("New URI: {}", newUri);

        ServerHttpRequest newRequest = request.mutate().uri(newUri).build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);
        log.info("Exchange attributes: {}", exchange.getAttributes());

        return chain.filter(exchange.mutate().request(newRequest).build()).then(Mono.defer(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.add("Tf-Source-IP", sourceIp);
            headers.add("Tf-Target-Address", targetAddress);
            return Mono.empty();
        }));
    }

    @Override
    public int getOrder() {
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }
}