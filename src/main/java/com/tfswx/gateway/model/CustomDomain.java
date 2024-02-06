package com.tfswx.gateway.model;

import com.tfswx.gateway.config.CustomDnsConstant;
import lombok.Data;

/**
 * 自定义域名
 *
 * @author: huojie
 * @date: 2024/02/02 16:26
 **/
@Data
public class CustomDomain {

    private String proejctName;

    private String engineeringName;

    /**
     * 获取完整域名
     */
    public String getTotalDomain() {
        return CustomDnsConstant.WWW + "." + CustomDnsConstant.RJSJPT + "." + proejctName + "." + engineeringName + "." + CustomDnsConstant.COM + ".";
    }

    /**
     * 获取缩写域名
     */
    public String getShortDomain() {
        return CustomDnsConstant.RJSJPT + "." + proejctName + "." + engineeringName + "." + CustomDnsConstant.COM + ".";
    }
}
