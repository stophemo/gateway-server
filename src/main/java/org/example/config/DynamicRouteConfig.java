package org.example.config;

import org.example.service.DynamicRouteService;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 动态路由配置
 *
 * @author: huojie
 * @date: 2024/01/09 17:02
 **/
@Configuration
public class DynamicRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, DynamicRouteService dynamicRouteService) {
        return builder.routes()
                .route("dynamic_route", r -> r.path("/dynamic/**")
                        .filters(f -> f.rewritePath("/dynamic/(?<segment>.*)", "/${segment}"))
                        .uri("lb://dynamic-service")
                )
                .build();
    }
}