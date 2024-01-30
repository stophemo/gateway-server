package org.example.service;

/**
 * 本地路由管理服务
 *
 * @author: huojie
 * @date: 2024/01/30 19:59
 **/
public interface LocalRouteService {
    String getTargetAddress(String project, String sourceIp, String path, boolean isDefault);

    void addRoute(String project, String sourceIp, String path, String targetAddress, boolean isDefault);

    void deleteRoute(String project, String sourceIp, String path, boolean isDefault);

    void updateRoute(String project, String sourceIp, String path, String targetAddress, boolean isDefault);

}
