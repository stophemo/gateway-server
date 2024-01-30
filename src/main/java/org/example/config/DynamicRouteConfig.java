//package org.example.config;
//
//import cn.hutool.core.collection.CollUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.example.model.Route;
//import org.example.service.RemoteRouteService;
//import org.example.util.RoutesStorageUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author: huojie
// * @date: 2024/01/30 17:14
// **/
//@Slf4j
//@Component
//public class DynamicRouteConfig {
//
//    @Autowired
//    private RouteDefinitionWriter routeDefinitionWriter;
//
//    @Resource
//    private RemoteRouteService remoteRouteService;
//
//    @PostConstruct
//    public void initRoutes() {
//        // 初始化路由信息
//        refreshRoutes();
//    }
//
//    /**
//     * 从控制端服务获取路由信息并更新
//     */
//    public void refreshRoutes() {
//        // 这里可以调用控制端服务的接口获取路由信息
//        List<RouteDefinition> routeDefinitions = loadRoutesFromExternalService();
//
//        // 将路由信息写入到RouteDefinitionWriter
//        for (RouteDefinition route : routeDefinitions) {
//            routeDefinitionWriter.save(Mono.just(route)).block();
//        }
//    }
//    /**
//     * 从控制端服务获取路由信息
//     */
//    private List<RouteDefinition> loadRoutesFromExternalService() {
//        // 假设从控制端服务获取到路由信息
//        List<RouteDefinition> routeDefinitions = new ArrayList<>();
//
//        List<Route> routeList = remoteRouteService.pullRemoteRoutes();
//        if (CollUtil.isEmpty(routeList)) {
//            log.warn("从控制端服务获取路由信息失败");
//            // 拉取失败，使用备份路由
//            routeList = RoutesStorageUtil.loadRoutes(Route.class);
//        } else {
//            // 拉取成功，备份路由信息
//            RoutesStorageUtil.backupRoutes(routeList);
//        }
//
//        // 将路由信息转换为RouteDefinition
//        routeList.forEach(route -> {
//            routeDefinitions.add(createRouteDefinition(
//                    route.getProject() + route.getNum(),
//                    route.getTargetAddress(),
//                    route.getPath(),
//                    route.getSourceIp()));
//        });
//
//        return routeDefinitions;
//    }
//
//    /**
//     * 将RouteConfig转换为RouteDefinition
//     */
//    private RouteDefinition createRouteDefinition(String id, String uri, String path, String ipAddress) {
//        RouteDefinition routeDefinition = new RouteDefinition();
//        routeDefinition.setId(id);
//        routeDefinition.setUri(URI.create(uri));
//        // 设置 Path 匹配谓词
//        routeDefinition.getPredicates().add(new PredicateDefinition("Path=" + path));
//        // 设置 RemoteAddr 匹配谓词
//        routeDefinition.getPredicates().add(new PredicateDefinition("RemoteAddr=" + ipAddress));
//        return routeDefinition;
//    }
//
//    /**
//     * 定时刷新，暂定15min
//     */
//    @Scheduled(fixedDelay = 900000)
//    public void scheduleRefreshRoutes() {
//        refreshRoutes();
//    }
//}
//
