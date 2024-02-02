package com.tfswx.gateway.model;

import lombok.Data;

@Data
public class DnsItem {
    /**
     * 域名
     */
    private String domain;
    /**
     * ip地址
     */
    private String ipAddress;
    /**
     * byte ip地址
     */
    private byte[] data;
}