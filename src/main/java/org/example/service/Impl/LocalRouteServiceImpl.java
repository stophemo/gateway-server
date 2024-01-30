package org.example.service;

import cn.hutool.core.util.StrUtil;
import org.example.config.GateWayConstant;
import org.example.model.RouteInfo;
import org.example.util.RoutesStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 本地路由管理服务实现
 *
 * @author: huojie
 * @date: 2024/01/30 20:04
 **/
@Service
public class LocalRouteServiceImpl implements LocalRouteService {

    private Map<String, Map<String, LinkedList<RouteInfo>>> routes;

    @Value("${storage.path}")
    private static String storagePath;

    @Resource
    private RemoteRouteService remoteRouteService;

    public LocalRouteServiceImpl() {
        this.routes = RoutesStorageUtil.loadRoutes(storagePath);
    }

    @PostConstruct
    public void initRoutes() {
        // 初始化默认路由必须的配置
    }

    @Override
    public void addRoute(String project, String sourceIp, String path, String targetAddress, boolean isDefault) {
        if (StrUtil.isNotBlank(project)) {
            Map<String, LinkedList<RouteInfo>> projectMap = routes.computeIfAbsent(project, k -> new HashMap<>());
            if (isDefault) {
                LinkedList<RouteInfo> routeInfoList = projectMap.computeIfAbsent(GateWayConstant.DEFAULT_ROUTE, k -> new LinkedList<>());
                routeInfoList.add(new RouteInfo(path, targetAddress));
            } else {
                if (StrUtil.isBlank(sourceIp)) {
                    throw new IllegalArgumentException("非默认配置,sourceIp不能为空");
                }
                LinkedList<RouteInfo> routeInfoList = projectMap.computeIfAbsent(sourceIp, k -> new LinkedList<>());
                routeInfoList.add(new RouteInfo(path, targetAddress));
            }
        }
    }

    @Override
    public void deleteRoute(String project, String sourceIp, String path, boolean isDefault) {
        if (project != null) {
            Map<String, LinkedList<RouteInfo>> projectMap = routes.get(project);
            if (projectMap != null) {
                if (isDefault) {
                    projectMap.remove(GateWayConstant.DEFAULT_ROUTE);
                } else {
                    // todo
                    LinkedList<RouteInfo> targetMap = projectMap.get(sourceIp);
                    if (targetMap != null) {
                        targetMap.remove(path);
                    }
                }
            }
        }


    }

    @Override
    public void updateRoute(String project, String sourceIp, String path, String targetAddress, boolean isDefault) {
        Map<String, Map<String, String>> projectMap = routes.get(project);
        if (projectMap != null) {
            if (isDefault) {
                Map<String, String> targetMap = projectMap.computeIfAbsent(GateWayConstant.DEFAULT_ROUTE, k -> new HashMap<>());
                targetMap.put(path, targetAddress);
            } else {
                Map<String, String> targetMap = projectMap.computeIfAbsent(sourceIp, k -> new HashMap<>());
                targetMap.put(path, targetAddress);
            }
        }
    }

    @Override
    public String getTargetAddress(String project, String sourceIp, String path, boolean isDefault) {
        Map<String, Map<String, String>> projectMap = routes.get(project);
        if (projectMap != null) {
            if (isDefault) {
                Map<String, String> targetMap = projectMap.get(GateWayConstant.DEFAULT_ROUTE);
                return (targetMap != null) ? targetMap.get(path) : null;
            } else {
                Map<String, String> targetMap = projectMap.get(sourceIp);
                return (targetMap != null) ? targetMap.get(path) : null;
            }
        }
        return null;
    }

}
