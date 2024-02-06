package com.tfswx.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目标地址获取入参
 *
 * @author: huojie
 * @date: 2024/01/31 10:59
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TargetAddressGetInputDTO {
    /**
     * 项目标识
     */
    private String project;
    /**
     * 源IP
     */
    private String sourceIp;
    /**
     * 匹配谓词path
     */
    private String path;
}
