package com.stophemo.gateway.service;

import com.stophemo.gateway.model.CustomDomain;
import com.stophemo.gateway.model.DnsItem;

import java.util.Map;

/**
 * 自定义DNS
 *
 * @author: huojie
 * @date: 2024/02/02 14:45
 **/
public interface CustomDnsService {

    Map<String, DnsItem> getBlackListDomain();

    void appendDnsItem(CustomDomain customDomain);

    void removeDnsItem(CustomDomain customDomain);

    DnsItem getDnsItem(String name);
}
