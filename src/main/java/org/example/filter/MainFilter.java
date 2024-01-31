package org.example.filter;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TargetAddressGetInputDTO;
import org.example.service.LocalRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
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
public class MainFilter implements GlobalFilter, Ordered {

    @Resource
    private LocalRouteService localRouteService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 保存请求方ip、目的地址到头信息
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String sourceIp = null;
        if (remoteAddress != null) {
            sourceIp = remoteAddress.getHostString();
            request.mutate().header("sourceIp", sourceIp);
        }
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (uri != null) {
            String targetAddress = uri.toString();
            request.mutate().header("targetAddress", targetAddress);
        }

        /* 将请求转发到目的地址
           浏览器请求的url地址示例为 http://192.168.7.30:8080/xxproject/xxpath  192.168.7.30:8080为当前的网关地址
           通过localRouteService.getTargetAddress(new TargetAddressGetInputDTO(xxproject,sourceIp,xxpath))获取目的地址
           参数从请求url中截取，获取到的目的地址类似为192.168.7.40:9999
         */
        String project = null;
        String path = null;
        if (uri != null) {
            project = uri.getPath().split("/")[1];
            path = uri.getPath().substring(uri.getPath().indexOf("/", 1));
        }
        String targetAddress = localRouteService.getTargetAddress(new TargetAddressGetInputDTO(project, sourceIp, path));
        if (targetAddress != null) {
            URI targetUri = URI.create(targetAddress);
            ServerHttpRequest newRequest = request.mutate().uri(targetUri).build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, targetUri);
            return chain.filter(exchange.mutate().request(newRequest).build());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}