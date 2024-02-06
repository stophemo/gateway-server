package com.tfswx.gateway.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
class ProxyUdp {
    private volatile Channel localChannel;
    private Channel proxyChannel;

    public void init() throws InterruptedException {
        EventLoopGroup proxyGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(proxyGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new DatagramDnsQueryEncoder())
                                .addLast(new DatagramDnsResponseDecoder())
                                .addLast(new SimpleChannelInboundHandler<DatagramDnsResponse>() {
                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) {
                                        log.info(ctx.channel().toString());
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) {
                                        DatagramDnsQuery dnsQuery = localChannel.attr(AttributeKey.<DatagramDnsQuery>valueOf(String.valueOf(msg.id()))).get();
                                        DnsQuestion question = msg.recordAt(DnsSection.QUESTION);
                                        DatagramDnsResponse dnsResponse = new DatagramDnsResponse(dnsQuery.recipient(), dnsQuery.sender(), msg.id());
                                        dnsResponse.addRecord(DnsSection.QUESTION, question);

                                        for (int i = 0, count = msg.count(DnsSection.ANSWER); i < count; i++) {
                                            DnsRecord record = msg.recordAt(DnsSection.ANSWER, i);
                                            if (record.type() == DnsRecordType.A) {
                                                // just print the IP after query
                                                DnsRawRecord raw = (DnsRawRecord) record;
                                                DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(
                                                        question.name(),
                                                        DnsRecordType.A, 600, Unpooled.wrappedBuffer(ByteBufUtil.getBytes(raw.content())));
                                                dnsResponse.addRecord(DnsSection.ANSWER, queryAnswer);
                                            }
                                        }

                                        localChannel.writeAndFlush(dnsResponse);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
                                        log.error(e.getMessage(), e);
                                    }
                                });

                    }
                });
        proxyChannel = b.bind(0).sync().addListener(future1 -> {
            log.info("绑定成功");
        }).channel();
    }

    public void send(String domain, int id, Channel localChannel) {
        this.localChannel = localChannel;
        DnsQuery query = new DatagramDnsQuery(null, new InetSocketAddress("192.168.7.42", 53), id).setRecord(
                DnsSection.QUESTION,
                new DefaultDnsQuestion(domain, DnsRecordType.A));
        this.proxyChannel.writeAndFlush(query);
    }
}