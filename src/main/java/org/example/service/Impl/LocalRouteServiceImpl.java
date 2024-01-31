package org.example.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.example.config.GateWayConstant;
import org.example.dto.RouteAddInputDTO;
import org.example.dto.RouteDeleteInputDTO;
import org.example.dto.RouteUpdateInputDTO;
import org.example.dto.TargetAddressGetInputDTO;
import org.example.model.BaseRoute;
import org.example.model.Route;
import org.example.service.LocalRouteService;
import org.example.service.RemoteRouteService;
import org.example.util.RoutesStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 本地路由管理服务实现
 *
 * @author: huojie
 * @date: 2024/01/30 20:04
 **/
@Service
public class LocalRouteServiceImpl implements LocalRouteService {

    private Map<String, Map<String, LinkedList<BaseRoute>>> routes;

    @Value("${storage.path}")
    private String storagePath;

    @Resource
    private RemoteRouteService remoteRouteService;

    @PostConstruct
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Override
    public void refreshRoutes() {
        List<Route> pulledList = null;
        // 从远程拉取路由配置

            pulledList = remoteRouteService.pullRemoteRoutes();

        if (CollUtil.isEmpty(pulledList)) {
            return;
        }

        // 重新组装为当前网关服务的路由结构
        Map<String, Map<String, LinkedList<BaseRoute>>> routeMap = pulledList.stream()
                .collect(Collectors.groupingBy(Route::getProject,
                        Collectors.groupingBy(route -> route.getDefaultFlag() ? "default" : route.getSourceIp(),
                                Collectors.mapping(route -> BeanUtil.copyProperties(route, BaseRoute.class),
                                        Collectors.toCollection(LinkedList::new)))));

        // 将routes保存到本地
        RoutesStorageUtil.saveRoutes(routeMap, storagePath);
    }

    @Override
    public void addRoute(RouteAddInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = RoutesStorageUtil.loadRoutes(storagePath);
        if (StrUtil.isNotBlank(inputDTO.getProject())) {
            Map<String, LinkedList<BaseRoute>> projectMap = routes.computeIfAbsent(inputDTO.getProject(), k -> new HashMap<>());
            if (inputDTO.getDefaultFlag()) {
                LinkedList<BaseRoute> baseRouteList = projectMap.computeIfAbsent(GateWayConstant.DEFAULT_ROUTE, k -> new LinkedList<>());
                baseRouteList.add(BeanUtil.copyProperties(inputDTO, BaseRoute.class));
            } else {
                if (StrUtil.isBlank(inputDTO.getSourceIp())) {
                    throw new IllegalArgumentException("非默认配置,sourceIp不可为空");
                }
                LinkedList<BaseRoute> baseRouteList = projectMap.computeIfAbsent(inputDTO.getSourceIp(), k -> new LinkedList<>());
                baseRouteList.add(BeanUtil.copyProperties(inputDTO, BaseRoute.class));
            }
        }
        RoutesStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public void updateRoute(RouteUpdateInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = RoutesStorageUtil.loadRoutes(storagePath);
        if (StrUtil.isNotBlank(inputDTO.getProject())) {
            Map<String, LinkedList<BaseRoute>> projectMap = routes.get(inputDTO.getProject());
            if (projectMap != null) {
                if (inputDTO.getDefaultFlag()) {
                    LinkedList<BaseRoute> baseRouteList = projectMap.get(GateWayConstant.DEFAULT_ROUTE);
                    if (baseRouteList != null) {
                        baseRouteList.removeIf(baseRoute -> baseRoute.getId().equals(inputDTO.getId()));
                        baseRouteList.add(BeanUtil.copyProperties(inputDTO, BaseRoute.class));
                    }
                } else {
                    if (StrUtil.isBlank(inputDTO.getSourceIp())) {
                        throw new IllegalArgumentException("非默认配置,sourceIp不可为空");
                    }
                    LinkedList<BaseRoute> baseRouteList = projectMap.get(inputDTO.getSourceIp());
                    if (baseRouteList != null) {
                        baseRouteList.removeIf(baseRoute -> baseRoute.getId().equals(inputDTO.getId()));
                        baseRouteList.add(BeanUtil.copyProperties(inputDTO, BaseRoute.class));
                    }
                }
            }
        }
        RoutesStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public void deleteRoute(RouteDeleteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = RoutesStorageUtil.loadRoutes(storagePath);
        if (StrUtil.isNotBlank(inputDTO.getProject())) {
            Map<String, LinkedList<BaseRoute>> projectMap = routes.get(inputDTO.getProject());
            if (projectMap != null) {
                if (inputDTO.getDefaultFlag()) {
                    LinkedList<BaseRoute> baseRouteList = projectMap.get(GateWayConstant.DEFAULT_ROUTE);
                    if (baseRouteList != null) {
                        baseRouteList.removeIf(baseRoute -> baseRoute.getId().equals(inputDTO.getId()));
                    }
                } else {
                    if (StrUtil.isBlank(inputDTO.getSourceIp())) {
                        throw new IllegalArgumentException("非默认配置,sourceIp不可为空");
                    }
                    LinkedList<BaseRoute> baseRouteList = projectMap.get(inputDTO.getSourceIp());
                    if (baseRouteList != null) {
                        baseRouteList.removeIf(baseRoute -> baseRoute.getId().equals(inputDTO.getId()));
                    }
                }
            }
        }
        RoutesStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public String getTargetAddress(TargetAddressGetInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        PathMatcher pathMatcher = new AntPathMatcher();
        routes = RoutesStorageUtil.loadRoutes(storagePath);
        if (StrUtil.isNotBlank(inputDTO.getProject())) {
            Map<String, LinkedList<BaseRoute>> projectMap = routes.get(inputDTO.getProject());
            if (projectMap != null) {
                LinkedList<BaseRoute> baseRouteList = projectMap.get(inputDTO.getSourceIp());
                List<BaseRoute> sortedList = CollUtil.sortByProperty(baseRouteList, "num");
                if (CollUtil.isNotEmpty(sortedList)) {
                    for (BaseRoute baseRoute : sortedList) {
                        if (pathMatcher.match(baseRoute.getPath(), inputDTO.getPath())) {
                            return baseRoute.getTargetAddress();
                        }
                    }
                }
                // 没匹配到，使用默认路由
                baseRouteList = projectMap.get(GateWayConstant.DEFAULT_ROUTE);
                sortedList = CollUtil.sortByProperty(baseRouteList, "num");
                if (CollUtil.isNotEmpty(sortedList)) {
                    for (BaseRoute baseRoute : sortedList) {
                        if (pathMatcher.match(baseRoute.getPath(), inputDTO.getPath())) {
                            return baseRoute.getTargetAddress();
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("未找到匹配的路由");
    }

}
