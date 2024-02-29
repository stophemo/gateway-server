package com.tfswx.gateway.startup;

import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author: huojie
 * @date: 2024/02/29 15:41
 **/
@Slf4j
@Component
public class GatewayReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port}")
    private String serverPort;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("--- 开发网关地址：http://{}:{}", NetUtil.getLocalhostStr(), serverPort);
    }
}
