package org.example.service;

import org.example.dto.RequestUrlGetInputDTO;

/**
 * 动态路由服务
 *
 * @author: huojie
 * @date: 2024/01/17 16:01
 **/
public interface LocalRouteService {

    /**
     * 获取请求路径
     *
     * @param inputDTO 入参
     * @return String
     */
    String getRequestUrl(RequestUrlGetInputDTO inputDTO);

    /**
     * 更新本地路由数据
     */
    boolean updateRoute();


//    void addRoute(String ipAddress, String projectName, String serviceAddress, String pathRegex);
//
//    void deleteRoute(String ipAddress, String projectName, String serviceAddress);
//
//    void updateRoute(String ipAddress, String projectName, String serviceAddress, String pathRegex);
}
