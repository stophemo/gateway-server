package com.tfswx.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.tfswx.gateway.model.Route;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 远端路由服务
 *
 * @author: huojie
 * @date: 2024/01/22 13:55
 **/
@Lazy
@FeignClient(name = "application-server", url = "${feign.client.config.default.url}")
public interface RemoteRouteService {

    @PostMapping(value = "api/lypzgl/pullLygzxx")
    JSONObject pullRemoteRoutesAsMap();

    default List<Route> pullRemoteRoutes() {
        JSONObject jsonObject = pullRemoteRoutesAsMap();
        // 将json对象中的data数组转为List<Route>
        return jsonObject.getJSONArray("data").toJavaList(Route.class);
    }
}
