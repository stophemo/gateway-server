package com.stophemo.gateway.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: huojie
 * @date: 2024/01/30 20:52
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseRoute {
    /**
     * 唯一标识
     */
    @JSONField(name = "id")
    private String id;
    /**
     * 匹配谓词path
     */
    @JSONField(name = "path")
    private String path;
    /**
     * 目标地址
     */
    @JSONField(name = "targetAddress")
    private String targetAddress;
    /**
     * 序号
     */
    @JSONField(name = "num")
    private Integer num;
}
