package com.tfswx.gateway.api;

import com.tfswx.gateway.model.CustomDomain;
import com.tfswx.gateway.service.CustomDnsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 自定义域名管理
 *
 * @author: huojie
 * @date: 2024/02/02 16:21
 **/
@RequestMapping("api/domain")
@RestController
public class CustomDnsController {

    @Resource
    private CustomDnsService customDnsService;

    @PostMapping("appendDnsItem")
    public void appendDnsItem(@RequestBody @Validated CustomDomain customDomain) {
        customDnsService.appendDnsItem(customDomain);
    }

    @PostMapping("removeDnsItem")
    public void removeDnsItem(@RequestBody @Validated CustomDomain customDomain) {
        customDnsService.removeDnsItem(customDomain);
    }
}
