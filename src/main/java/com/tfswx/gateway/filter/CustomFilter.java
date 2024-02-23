package com.tfswx.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.tfswx.gateway.config.CustomDnsConstant;
import com.tfswx.gateway.config.GateWayConstant;
import com.tfswx.gateway.dto.TargetAddressGetInputDTO;
import com.tfswx.gateway.service.LocalRouteService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 自定义过滤器
 *
 * @author: huojie
 * @date: 2024/01/25 10:30
 **/
@Slf4j
@Component
public class CustomFilter implements GlobalFilter, Ordered {

    private final static List<String> DISTINCT_HEADERS = Collections.unmodifiableList(
            Arrays.asList(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                    HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS
            ));

    @Resource
    private LocalRouteService localRouteService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.保存请求方ip、目的地址到头信息
        // 2.将请求转发到目的地址
        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        log.info("----------------------------------");
        log.info("Request address: {}", requestUri);

        String host = requestUri.getHost();
        String path = requestUri.getPath();
        // 浏览器图标请求不做处理
        if (GateWayConstant.FAVICON_PATH.equals(path)) {
            return chain.filter(exchange);
        }

        String engineeringName;
        if (StrUtil.startWith(host, CustomDnsConstant.RJSJPT)
                || StrUtil.startWith(host, CustomDnsConstant.WWW + "." + CustomDnsConstant.RJSJPT)
                && StrUtil.endWith(host, CustomDnsConstant.COM)) {
            String[] parts = host.split("\\.");
            engineeringName = parts[parts.length - 2];
        } else {
            return chain.filter(exchange);
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String sourceIp;
        if (remoteAddress != null) {
            sourceIp = remoteAddress.getAddress().getHostAddress();
        } else {
            sourceIp = null;
        }

        TargetAddressGetInputDTO inputDTO = new TargetAddressGetInputDTO(engineeringName, sourceIp, path);
        String targetAddress = localRouteService.getTargetAddress(inputDTO);
        log.info("Target  address: {}", targetAddress);

        URI newUri = UriComponentsBuilder.fromHttpUrl(targetAddress + path).build().toUri();
        log.info("New         URI: {}", newUri);
        ServerHttpRequest newRequest = request.mutate().uri(newUri).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);

        return chain.filter(exchange.mutate().request(newRequest).build()).then(Mono.defer(() -> {
            exchange.getResponse().getHeaders().entrySet().stream()
                    // 跨域请求头重复处理
                    .filter(kv -> DISTINCT_HEADERS.contains(kv.getKey()) && kv.getValue() != null && kv.getValue().size() > 1)
                    .forEach(kv -> {
                        List<String> list = new ArrayList<>(1);
                        list.add(kv.getValue().stream().filter(StrUtil::isNotBlank).findFirst().orElse(null));
                        kv.setValue(list);
                    });
            return Mono.empty();
        })).then(Mono.defer(() -> {
            HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
            if (!isWebSocketRequest(requestHeaders)) {
                // 如果不是 WebSocket 连接请求，添加自定义的响应头
                HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
                responseHeaders.add("Tf-Source-IP", sourceIp);
                responseHeaders.add("Tf-Target-Address", targetAddress);
            }
            return Mono.empty();
        }));
    }

    @Override
    public int getOrder() {
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }

    /**
     * 判断是否websocket连接请求
     *
     * @param requestHeaders 请求头
     * @return 布尔值
     */
    private boolean isWebSocketRequest(HttpHeaders requestHeaders) {
        return requestHeaders.containsKey("Upgrade") && requestHeaders.containsKey("Connection") &&
                "websocket".equalsIgnoreCase(requestHeaders.getFirst("Upgrade")) &&
                "upgrade".equalsIgnoreCase(requestHeaders.getFirst("Connection"));
    }

}