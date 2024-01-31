package org.example.model;

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
    private Integer num;
}
