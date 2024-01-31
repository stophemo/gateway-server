package org.example.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.TargetAddressGetInputDTO;
import org.example.service.LocalRouteService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
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
 * 添加请求方ip到请求头,目的地址到响应头
 *
 * @author: huojie
 * @date: 2024/01/25 10:30
 **/
@Slf4j
@Component
public class MainFilter implements GlobalFilter, Ordered {

    @Resource
    private LocalRouteService localRouteService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.保存请求方ip、目的地址到头信息
        // 2.将请求转发到目的地址

        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String path = requestUri.getPath();

        // 从请求URL中截取项目名和路径
        String[] parts = path.split("/");
        String project = parts[1];
        String pathInProject = "/" + String.join("/", Arrays.copyOfRange(parts, 2, parts.length));

        // 获取请求方IP
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String sourceIp = remoteAddress.getAddress().getHostAddress();

        // 调用localRouteService.getTargetAddress获取目标地址
        TargetAddressGetInputDTO inputDTO = new TargetAddressGetInputDTO(project, sourceIp, pathInProject);
        String targetAddress = localRouteService.getTargetAddress(inputDTO);

        // 构建新的请求URI
        URI newUri = UriComponentsBuilder.fromHttpUrl(targetAddress + pathInProject).build().toUri();

        // 创建新的请求，并在请求头中添加请求方IP和目标地址
        ServerHttpRequest newRequest = request.mutate()
                .uri(newUri)
                .header("X-Source-IP", sourceIp)
                .header("X-Target-Address", targetAddress)
                .build();

        // 将新的请求添加到exchange中
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);

        return chain.filter(exchange.mutate().request(newRequest).build());

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}