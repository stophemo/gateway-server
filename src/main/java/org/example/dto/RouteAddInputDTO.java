package org.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加路由入参
 *
 * @author: huojie
 * @date: 2024/01/31 10:33
 **/
@Data
public class RouteAddInputDTO {
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
    private boolean isDefault;
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
