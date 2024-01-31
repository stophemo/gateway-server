package org.example.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.config.GateWayConstant;
import org.example.model.BaseRoute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

@Slf4j
public class RoutesStorageUtil {

    public static Map<String, Map<String, LinkedList<BaseRoute>>> loadRoutes(String storagePath) {
        try {
            Path filePath = Paths.get(getStoragePath(storagePath));
            if (Files.exists(filePath)) {
                String content = new String(Files.readAllBytes(filePath));
                return JSON.parseObject(content, new TypeReference<Map<String, Map<String, LinkedList<BaseRoute>>>>() {
                });
            }
        } catch (IOException e) {
            log.error("从文件加载路由时出错", e);
        }
        return null;
    }

    public static void saveRoutes(Map<String, Map<String, LinkedList<BaseRoute>>> routes, String storagePath) {
        try {
            String json = JSON.toJSONString(routes);
            Files.write(Paths.get(getStoragePath(storagePath)), json.getBytes());
        } catch (IOException e) {
            log.error("保存路由到文件时出错", e);
        }
    }

    private static String getStoragePath(String storagePath) {
        return storagePath + "/" + GateWayConstant.ROUTES + ".json";
    }
}

