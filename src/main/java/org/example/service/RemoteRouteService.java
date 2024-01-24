package org.example.service;

import com.tfswx.component.resttemplateplus.TfRestController;
import io.swagger.annotations.ApiOperation;
import org.example.dto.RequestUrlGetInputDTO;
import org.example.model.RouteConfig;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 远端路由服务
 *
 * @author: huojie
 * @date: 2024/01/22 13:55
 **/
@TfRestController
public interface RemoteRouteService {

    @ApiOperation("拉取远端路由数据")
    @PostMapping(value = "api/lypzgl/pullLypzxx")
    List<RouteConfig> pullRemoteRoute();

    @ApiOperation("获取路由uri")
    @PostMapping(value = "api/lypzgl/getLyuri")
    String getGatewayRequestUrl(RequestUrlGetInputDTO inputDTO);
}
