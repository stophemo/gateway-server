package com.stophemo.gateway.api;

import com.stophemo.gateway.service.LocalRouteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 路由管理对外接口
 *
 * @author: huojie
 * @date: 2024/02/01 19:57
 **/
@RequestMapping("api/gateway")
@RestController
public class RoutesController {

    @Resource
    private LocalRouteService localRouteService;

    @PostMapping("refreshRoutes")
    public void refreshRoutes() {
        localRouteService.refreshRoutes();
    }

}
