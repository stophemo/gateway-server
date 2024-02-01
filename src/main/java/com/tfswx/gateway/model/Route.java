package com.tfswx.gateway.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 路由规则定义
 *
 * @author: huojie
 * @date: 2024/01/22 14:13
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class Route extends BaseRoute {

    /**
     * 项目标识
     */
    @JSONField(name = "project")
    private String project;
    /**
     * 源IP
     */
    @JSONField(name = "sourceIp")
    private String sourceIp;

    /**
     * 是否是默认路由
     */
    @JSONField(name = "defaultFlag")
    private Boolean defaultFlag;
}
