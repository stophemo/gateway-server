package org.example.model;

import lombok.Data;

/**
 * 用户路由配置
 *
 * @author: huojie
 * @date: 2024/01/22 14:13
 **/
@Data
public class RouteConfig {

    /**
     * 配置id
     */
    private String configId;
    /**
     * 用户ip
     */
    private String ipAddress;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 服务地址
     */
    private String serviceAddress;
    /**
     * 路径正则
     */
    private String pathRegex;
    /**
     * 优先级序号
     */
    private int priorityNum;

}
