package org.example.service;

import org.example.dto.RequestUrlGetInputDTO;
import org.example.model.Route;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 远端路由服务
 *
 * @author: huojie
 * @date: 2024/01/22 13:55
 **/
@FeignClient(name = "application-server", url = "http://192.168.7.30:29090")
public interface RemoteRouteService {

    @PostMapping(value = "api/lypzgl/pullLygzxx")
    List<Route> pullRemoteRoutes();

    @PostMapping(value = "api/lypzgl/getLygzxx")
    String getGatewayRequestUrl(RequestUrlGetInputDTO inputDTO);
}
