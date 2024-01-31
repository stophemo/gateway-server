package org.example.service;

import com.alibaba.fastjson.JSONObject;
import org.example.dto.RequestUrlGetInputDTO;
import org.example.model.Route;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 远端路由服务
 *
 * @author: huojie
 * @date: 2024/01/22 13:55
 **/
@FeignClient(name = "application-server", url = "http://192.168.1.85:9090")
public interface RemoteRouteService {

    @PostMapping(value = "api/lypzgl/pullLygzxx")
    JSONObject pullRemoteRoutesAsMap();

    default List<Route> pullRemoteRoutes() {
        JSONObject jsonObject = pullRemoteRoutesAsMap();
        // 将json对象中的data数组转为List<Route>
        return jsonObject.getJSONArray("data").toJavaList(Route.class);
    }
}
