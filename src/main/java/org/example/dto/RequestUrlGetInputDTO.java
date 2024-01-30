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
    @NotBlank(message = "项目简称不能为空")
    private String project;

    private String sourceIp;

    @NotBlank(message = "具体路径路径不能为空")
    private String path;
}
