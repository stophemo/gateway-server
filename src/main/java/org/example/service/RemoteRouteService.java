package org.example.service;

import com.tfswx.component.resttemplateplus.TfRestController;
import com.tfswx.component.resttemplateplus.web.TfPostMapping;
import org.example.dto.RequestUrlGetInputDTO;
import org.example.model.RouteConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远端路由服务
 *
 * @author: huojie
 * @date: 2024/01/22 13:55
 **/
@TfRestController
public interface RemoteRouteService {

    @TfPostMapping(value = "api/lypzgl/pullLypzxx")
    List<RouteConfig> pullRemoteRoute();

    @TfPostMapping(value = "api/lypzgl/getLyuri")
    String getGatewayRequestUrl(RequestUrlGetInputDTO inputDTO);
}
