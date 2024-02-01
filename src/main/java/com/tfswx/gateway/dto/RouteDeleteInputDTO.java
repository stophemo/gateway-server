package com.tfswx.gateway.dto;

import lombok.Data;

/**
 * 路由删除入参
 *
 * @author: huojie
 * @date: 2024/01/31 10:34
 **/
@Data
public class RouteDeleteInputDTO {
    /**
     * 项目标识
     */
    private String project;
    /**
     * 源IP
     */
    private String sourceIp;
    /**
     * 是否是默认路由
     */
    private Boolean defaultFlag;
    /**
     * 唯一标识
     */
    private String id;
}
