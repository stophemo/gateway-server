package org.example.model;

import lombok.Data;

/**
 * 路由规则定义
 *
 * @author: huojie
 * @date: 2024/01/22 14:13
 **/
@Data
public class Route {

    private Integer num;

    private String project;

    private String sourceIp;

    private String Path;

    private String targetAddress;

    private String isDefault;
}
