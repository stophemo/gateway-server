package org.example.service.impl;

import cn.hutool.core.collection.CollUtil;
import org.example.dto.RequestUrlGetInputDTO;
import org.example.model.RouteConfig;
import org.example.service.LocalRouteService;
import org.example.service.RemoteRouteService;
import org.example.util.TreeFileStorageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态路由服务
 *
 * @author: huojie
 * @date: 2024/01/09 17:03
 **/
@Service
public class LocalRouteServiceImpl implements LocalRouteService {

    @Resource
    private RemoteRouteService remoteRouteService;

    @Override
    public String getRequestUrl(RequestUrlGetInputDTO inputDTO) {
        List<RouteConfig> routeConfigList = TreeFileStorageUtil.loadTree(RouteConfig.class);
        if (CollUtil.isEmpty(routeConfigList)) {
            return null;
        }

        String result = null;
        for (RouteConfig routeConfig : routeConfigList) {
            if (inputDTO.getIpAddress().equals(routeConfig.getIpAddress())
                    && inputDTO.getProjectName().equals(routeConfig.getProjectName())) {
                continue;
            }
            if (inputDTO.getPath().matches(routeConfig.getPathRegex())) {
                result = routeConfig.getServiceAddress();
            }
        }
        return result;
    }

    @Override
    public boolean updateRoute() {
        List<RouteConfig> routeConfigList = remoteRouteService.pullRemoteRoute();
        if (CollUtil.isEmpty(routeConfigList)) {
            return false;
        }

        // 排序
        List<RouteConfig> sortedRouteConfigList = routeConfigList.stream()
                .sorted(Comparator.comparing((RouteConfig rc) -> rc.getIpAddress().equals("default") ? 0 : 1,
                                Comparator.naturalOrder())
                        .thenComparing(RouteConfig::getProjectName)
                        .thenComparing(RouteConfig::getPriorityNum, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        TreeFileStorageUtil.saveTree(sortedRouteConfigList);
        return true;
    }


//    @Override
//    public void addRoute(String ipAddress, String project, String serviceAddress, String pathRegex) {
//        UserRouteTree userRouteTree = TreeFileStorageUtil.loadTree(UserRouteTree.class);
//        userRouteTree.addRoute(ipAddress, project, serviceAddress, pathRegex);
//        TreeFileStorageUtil.saveTree(userRouteTree);
//    }
//
//    @Override
//    public void deleteRoute(String ipAddress, String project, String serviceAddress) {
//        UserRouteTree userRouteTree = TreeFileStorageUtil.loadTree(UserRouteTree.class);
//        userRouteTree.deleteRoute(ipAddress, project, serviceAddress);
//        TreeFileStorageUtil.saveTree(userRouteTree);
//    }
//
//    @Override
//    public void updateRoute(String ipAddress, String project, String serviceAddress, String pathRegex) {
//        UserRouteTree userRouteTree = TreeFileStorageUtil.loadTree(UserRouteTree.class);
//        userRouteTree.updateRoute(ipAddress, project, serviceAddress, pathRegex);
//        TreeFileStorageUtil.saveTree(userRouteTree);
//    }
}
