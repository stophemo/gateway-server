package com.tfswx.gateway.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 路由更新入参
 *
 * @author: huojie
 * @date: 2024/01/31 10:34
 **/
@Data
public class RouteUpdateInputDTO {
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
     * 路由ID
     */
    private String id;
    /**
     * 匹配谓词path
     */
    private String path;
    /**
     * 目标地址
     */
    private String targetAddress;
    /**
     * 序号
     */
    private int num;
}
