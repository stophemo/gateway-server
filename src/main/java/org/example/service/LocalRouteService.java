package org.example.service;

import org.example.dto.RouteAddInputDTO;
import org.example.dto.RouteDeleteInputDTO;
import org.example.dto.RouteUpdateInputDTO;
import org.example.dto.TargetAddressGetInputDTO;

/**
 * 本地路由管理服务
 *
 * @author: huojie
 * @date: 2024/01/30 19:59
 **/
public interface LocalRouteService {

    void addRoute(RouteAddInputDTO inputDTO);

    void updateRoute(RouteUpdateInputDTO inputDTO);

    void deleteRoute(RouteDeleteInputDTO inputDTO);

    String getTargetAddress(TargetAddressGetInputDTO inputDTO);

}
