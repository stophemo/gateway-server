package com.stophemo.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.stophemo.gateway.dto.TargetAddressGetInputDTO;
import com.stophemo.gateway.config.CustomDnsConstant;
import com.stophemo.gateway.service.LocalRouteService;
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
        String host = requestUri.getHost();
        String path = requestUri.getPath();
        String engineeringName;
        if (StrUtil.startWith(host, CustomDnsConstant.RJSJPT)
                || StrUtil.startWith(host, CustomDnsConstant.WWW + "." + CustomDnsConstant.RJSJPT)
                && StrUtil.endWith(host, CustomDnsConstant.COM)) {
            // 取.com前面的字符串
            String[] parts = host.split("\\.");
            engineeringName = parts[parts.length - 1];
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
        log.info("Target address: {}", targetAddress);

        URI newUri = UriComponentsBuilder.fromHttpUrl(targetAddress + path).build().toUri();
        log.info("New URI: {}", newUri);

        ServerHttpRequest newRequest = request.mutate().uri(newUri).build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);
        log.info("Exchange attributes: {}", exchange.getAttributes());

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