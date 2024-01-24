package org.example.filter;

import cn.hutool.core.util.StrUtil;
import org.example.dto.RequestUrlGetInputDTO;
import org.example.service.LocalRouteService;
import org.example.service.RemoteRouteService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Objects;

/**
 * 用户路由网管过滤器工厂
 *
 * @author: huojie
 * @date: 2024/01/10 10:25
 **/
public class CustomRoutingGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRoutingGatewayFilterFactory.Config> {

    @Resource
    private LocalRouteService localRouteService;

    @Resource
    private RemoteRouteService remoteRouteService;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String remoteIp = getRemoteIp(exchange);
            URI serverUri = getTargetRouteForIp(remoteIp, exchange.getRequest().getURI());
            ServerWebExchange modifiedExchange = exchange.mutate().request(builder -> builder.uri(serverUri)).build();
            return chain.filter(modifiedExchange);
        };
    }

    private String getRemoteIp(ServerWebExchange exchange) {
        // 获取请求方IP的代码
        ServerHttpRequest request = exchange.getRequest();
        return Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
    }

    private URI getTargetRouteForIp(String remoteIp, URI originalUri) {
        // 从原始URI中解析服务名和具体路径
        String path = originalUri.getPath();
        String[] segments = path.split("/", 3);
        String serviceName = segments[1]; // 服务名
        String specificPath = segments[2]; // 具体路径

        // 根据请求方IP和解析出的服务名、具体路径从存储中获取对应的路由配置
        // 返回对应的路由地址
        String requestUrl = remoteRouteService.getGatewayRequestUrl(new RequestUrlGetInputDTO(remoteIp, serviceName, specificPath));
        if (StrUtil.isBlank(requestUrl)) {
            requestUrl = localRouteService.getRequestUrl(new RequestUrlGetInputDTO(remoteIp, serviceName, specificPath));
        }
        return URI.create(requestUrl);
    }

    public static class Config {
        // 可以在这里定义配置参数
    }
}