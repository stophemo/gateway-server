package com.stophemo.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
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