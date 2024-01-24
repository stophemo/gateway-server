package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * RequestUrl获取入参
 *
 * @author: huojie
 * @date: 2024/01/22 15:21
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUrlGetInputDTO {
    /**
     * 用户ip
     */
    @NotBlank(message = "请求方ip不能为空")
    private String ipAddress;
    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    /**
     * 路径
     */
    @NotBlank(message = "请求路径不能为空")
    private String path;
}
