package com.tfswx.gateway.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.tfswx.gateway.config.CustomDnsConstant;
import com.tfswx.gateway.model.CustomDomain;
import com.tfswx.gateway.model.DnsItem;
import com.tfswx.gateway.service.CustomDnsService;
import com.tfswx.gateway.util.GatewayStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义DNS
 *
 * @author: huojie
 * @date: 2024/02/02 14:45
 **/
@Slf4j
@Service
public class CustomDnsServiceImpl implements CustomDnsService {

    private volatile Map<String, DnsItem> dnsItemMap;
    private String localIp;

    @Value("${storage.path}")
    private String storagePath;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        if (GatewayStorageUtil.loadDnsMap(storagePath) == null) {
            dnsItemMap = new HashMap<>();
        } else {
            dnsItemMap = GatewayStorageUtil.loadDnsMap(storagePath);
        }
        localIp = getLocalIp();
        log.info("开发网关服务器地址为：{}", localIp);
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void saveDnsMap() {
        GatewayStorageUtil.saveDnsMap(dnsItemMap, storagePath);
    }

    @Override
    public Map<String, DnsItem> getBlackListDomain() {
        return dnsItemMap;
    }

    @Override
    public void appendDnsItem(CustomDomain customDomain) {
        if (StrUtil.isBlank(customDomain.getProejctName()) || StrUtil.isBlank(customDomain.getEngineeringName())) {
            throw new IllegalArgumentException("项目名和工程名不能为空");
        }
        byte[] parseIpAddress = parseIpAddress(localIp);
        append(customDomain.getTotalDomain(), parseIpAddress);
        append(customDomain.getShortDomain(), parseIpAddress);
        GatewayStorageUtil.saveDnsMap(dnsItemMap, storagePath);
    }

    @Override
    public void removeDnsItem(CustomDomain customDomain) {
        if (StrUtil.isBlank(customDomain.getProejctName()) || StrUtil.isBlank(customDomain.getEngineeringName())) {
            throw new IllegalArgumentException("项目名和工程名不能为空");
        }
        dnsItemMap.remove(customDomain.getTotalDomain());
        dnsItemMap.remove(customDomain.getShortDomain());
    }

    @Override
    public DnsItem getDnsItem(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }

        if (StrUtil.startWith(name, CustomDnsConstant.RJSJPT)
                || StrUtil.startWith(name, CustomDnsConstant.WWW + "." + CustomDnsConstant.RJSJPT)) {

            DnsItem dnsItem = new DnsItem(name, localIp, parseIpAddress(localIp));
            dnsItemMap.putIfAbsent(name, dnsItem);
            return dnsItem;

        } else {
            return dnsItemMap.get(name);
        }
    }

    /**
     * 获取本机ip，
     * 多网卡或获取失败提示需要手动配置，
     * 从yml配置属性 dns.ip 中获取
     *
     * @return 本机ip
     */
    private String getLocalIp() {
        String gatewayIp = environment.getProperty("dns.ip");
        // 如果配置了dns.gateway.ip属性，则使用该配置
        if (gatewayIp != null && !gatewayIp.isEmpty()) {
            return gatewayIp;
        }

        // 如果没有配置dns.gateway.ip属性，则尝试获取本机IP
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        // 获取ip地址
                        gatewayIp = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("获取本机IP失败，请手动配置dns.gateway.ip属性", e);
        }

        // 返回空数组或者抛出异常，表示获取失败
        log.info("dns服务器地址为：{}", gatewayIp);
        return gatewayIp;
    }

    private byte[] parseIpAddress(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.getAddress();
        } catch (Exception e) {
            throw new RuntimeException("解析ip地址失败", e);
        }
    }

    private void append(String domain, byte[] ipBuf) {
        DnsItem dnsItem = new DnsItem();
        dnsItem.setDomain(domain);
        dnsItem.setData(ipBuf);
        dnsItem.setIpAddress(String.format("%s.%s.%s.%s",
                Byte.toUnsignedInt(ipBuf[0]),
                Byte.toUnsignedInt(ipBuf[1]),
                Byte.toUnsignedInt(ipBuf[2]),
                Byte.toUnsignedInt(ipBuf[3])
        ));
        dnsItemMap.put(dnsItem.getDomain(), dnsItem);
    }
}
