package com.tfswx.gateway.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tfswx.futool.core.thread.concurrent.ConcurrentCallMethodMerge;
import com.tfswx.gateway.config.GateWayConstant;
import com.tfswx.gateway.model.BaseRoute;
import com.tfswx.gateway.model.Route;
import com.tfswx.gateway.service.LocalRouteService;
import com.tfswx.gateway.util.GatewayStorageUtil;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import com.tfswx.gateway.dto.RouteAddInputDTO;
import com.tfswx.gateway.dto.RouteDeleteInputDTO;
import com.tfswx.gateway.dto.RouteUpdateInputDTO;
import com.tfswx.gateway.dto.TargetAddressGetInputDTO;
import com.tfswx.gateway.service.RemoteRouteService;
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
@Slf4j
@Service
public class LocalRouteServiceImpl implements LocalRouteService {

    private volatile Map<String, Map<String, LinkedList<BaseRoute>>> routes;

    @Value("${storage.path}")
    private String storagePath;

    @Resource
    private RemoteRouteService remoteRouteService;

    private final ConcurrentCallMethodMerge callMethodMerge;

    public LocalRouteServiceImpl() {
        callMethodMerge = new ConcurrentCallMethodMerge(1, this::refreshRoutesHandler);
    }

    private void refreshRoutesHandler() {
        List<Route> pulledList = null;
        // 从远程拉取路由配置
        try {
            pulledList = remoteRouteService.pullRemoteRoutes();
        } catch (FeignException feignException) {
            log.warn("Feign拉取路由配置 请求失败，状态码：{}, 请求URL：{}", feignException.status(), feignException.request().url(), feignException);
            routes = GatewayStorageUtil.loadRoutes(storagePath);
        }

        if (CollUtil.isEmpty(pulledList)) {
            return;
        }

        // 过滤掉无效路由数据、重新组装为当前网关服务的路由结构
        routes = pulledList.stream()
                .filter(route -> StrUtil.isNotBlank(route.getProject())
                        && StrUtil.isNotBlank(route.getId())
                        && route.getDefaultFlag() != null
                        && route.getDefaultFlag() || StrUtil.isNotBlank(route.getSourceIp()))
                .collect(Collectors.groupingBy(Route::getProject,
                        Collectors.groupingBy(route -> route.getDefaultFlag() ? "default" : route.getSourceIp(),
                                Collectors.mapping(route -> BeanUtil.copyProperties(route, BaseRoute.class),
                                        Collectors.toCollection(LinkedList::new)))));

        // 将routes保存到本地
        GatewayStorageUtil.saveRoutes(routes, storagePath);
    }

    /**
     * 初始化，定时更新
     */
    @PostConstruct
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Override
    public void refreshRoutes() {
        callMethodMerge.call();
    }

    @Override
    public void addRoute(RouteAddInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = GatewayStorageUtil.loadRoutes(storagePath);
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
        GatewayStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public void updateRoute(RouteUpdateInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = GatewayStorageUtil.loadRoutes(storagePath);
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
        GatewayStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public void deleteRoute(RouteDeleteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        routes = GatewayStorageUtil.loadRoutes(storagePath);
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
        GatewayStorageUtil.saveRoutes(routes, storagePath);
    }

    @Override
    public String getTargetAddress(TargetAddressGetInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("入参对象不可为空");
        }
        PathMatcher pathMatcher = new AntPathMatcher();
//        routes = RoutesStorageUtil.loadRoutes(storagePath);
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
