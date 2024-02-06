package com.stophemo.gateway.service;

import com.stophemo.gateway.dto.RouteAddInputDTO;
import com.stophemo.gateway.dto.RouteDeleteInputDTO;
import com.stophemo.gateway.dto.RouteUpdateInputDTO;
import com.stophemo.gateway.dto.TargetAddressGetInputDTO;

/**
 * 本地路由管理服务
 *
 * @author: huojie
 * @date: 2024/01/30 19:59
 **/
public interface LocalRouteService {

    void refreshRoutes();

    void addRoute(RouteAddInputDTO inputDTO);

    void updateRoute(RouteUpdateInputDTO inputDTO);

    void deleteRoute(RouteDeleteInputDTO inputDTO);

    String getTargetAddress(TargetAddressGetInputDTO inputDTO);

}
