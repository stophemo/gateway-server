package com.tfswx.gateway.service;

import com.tfswx.gateway.dto.RouteAddInputDTO;
import com.tfswx.gateway.dto.RouteDeleteInputDTO;
import com.tfswx.gateway.dto.RouteUpdateInputDTO;
import com.tfswx.gateway.dto.TargetAddressGetInputDTO;

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
