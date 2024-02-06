package com.stophemo.gateway.dns;

import com.stophemo.gateway.model.DnsItem;
import com.stophemo.gateway.service.CustomDnsService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author CYVATION-LXL
 */
@Slf4j
@Component
public final class DnsServer implements InitializingBean {

    @Autowired
    private Environment environment;

    @Resource
    private CustomDnsService customDnsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        ProxyUdp proxyUdp = new ProxyUdp();
        proxyUdp.init();
        final NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) {
                        nioDatagramChannel.pipeline().addLast(new DatagramDnsQueryDecoder());
                        nioDatagramChannel.pipeline().addLast(new SimpleChannelInboundHandler<DatagramDnsQuery>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery msg) {
                                try {
                                    DefaultDnsQuestion dnsQuestion = msg.recordAt(DnsSection.QUESTION);
                                    String name = dnsQuestion.name();
                                    Channel channel = ctx.channel();
                                    int id = msg.id();
                                    channel.attr(AttributeKey.<DatagramDnsQuery>valueOf(String.valueOf(id))).set(msg);
                                    DnsItem dnsItem = customDnsService.getDnsItem(name);
                                    if (dnsItem != null) {
                                        DnsQuestion question = msg.recordAt(DnsSection.QUESTION);
                                        DatagramDnsResponse dnsResponse = getDatagramDnsResponse(msg, id, question, dnsItem.getData());
                                        channel.writeAndFlush(dnsResponse);
                                        log.info("匹配域名：{} -> {}", name, dnsItem.getIpAddress());
                                    } else {
//                                        channel.close().sync();
                                        proxyUdp.send(name, msg.id(), channel);
//                                        log.warn("未知域名：{}", name);
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }

                            private DatagramDnsResponse getDatagramDnsResponse(DatagramDnsQuery msg, int id, DnsQuestion question, byte[] destIp) {
                                DatagramDnsResponse dnsResponse = new DatagramDnsResponse(msg.recipient(), msg.sender(), id);
                                dnsResponse.addRecord(DnsSection.QUESTION, question);

                                // just print the IP after query
                                DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(
                                        question.name(),
                                        DnsRecordType.A, 600, Unpooled.wrappedBuffer(destIp));
                                dnsResponse.addRecord(DnsSection.ANSWER, queryAnswer);
                                return dnsResponse;
                            }
//                            private DatagramDnsResponse getDatagramDnsResponse(DatagramDnsQuery msg, int id, DnsQuestion question, byte[] destIp) {
//                                int destPort = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
//
//                                DatagramDnsResponse dnsResponse = new DatagramDnsResponse(msg.recipient(), msg.sender(), id);
//                                dnsResponse.addRecord(DnsSection.QUESTION, question);
//
//                                // Add IP address to the ANSWER section
//                                DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(
//                                        question.name(),
//                                        DnsRecordType.A, 600, Unpooled.wrappedBuffer(destIp));
//                                dnsResponse.addRecord(DnsSection.ANSWER, queryAnswer);
//
//                                // Add custom port record, using SRV record type
//                                ByteBuf portBuffer = Unpooled.buffer(2);
//                                portBuffer.writeShortLE(destPort);
//                                DnsRawRecord portRecord = new DefaultDnsRawRecord(
//                                        question.name(),
//                                        DnsRecordType.SRV, 600, portBuffer);
//                                dnsResponse.addRecord(DnsSection.ANSWER, portRecord);
//
//                                return dnsResponse;
//                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
                                log.error(e.getMessage(), e);
                            }
                        });
                        nioDatagramChannel.pipeline().addLast(new DatagramDnsResponseEncoder());

                    }
                }).option(ChannelOption.SO_BROADCAST, true);

        int port = 53;
        ChannelFuture future = bootstrap.bind(port).addListener(future1 -> {
            log.info("dns server listening port:{}", port);
        });

        future.channel().closeFuture().addListener(future1 -> {
            if (future.isSuccess()) {
                log.info("dns listener closed: {}", future.channel());
            }
        });
    }
}