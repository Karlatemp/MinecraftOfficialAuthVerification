/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:42:29
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/FakeServer.java
 */

package cn.mcres.karlatemp.moav;

import cn.mcres.karlatemp.moav.event.LoginSuccessEvent;
import cn.mcres.karlatemp.moav.event.PlayerListPingEvent;
import cn.mcres.karlatemp.moav.util.ArrayBuilder;
import cn.mcres.karlatemp.moav.util.EncryptionRequest;
import cn.mcres.karlatemp.moav.util.ObjectBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class FakeServer extends ChannelInboundHandlerAdapter {
    public static final String VERSION = "1.0";
    public static final AttributeKey<ChannelOptions> co =
            AttributeKey.valueOf("Karlatemp-MOAV-Options");
    public static boolean DEBUG = Boolean.getBoolean("moav.debug");
    public static boolean EXIT = !Boolean.getBoolean("moav.noexit");

    public static int PORT = Integer.getInteger("moav.port", 1145);

    public static void main(String[] args) {
        LoginSuccessEvent.BUS.register(event -> {
            // Disconnect with custom message.
            // 使用自定义的信息断开连接
            // @see https://minecraft.gamepedia.com/Raw_JSON_text_format
            // @see https://minecraft-zh.gamepedia.com/%E5%8E%9F%E5%A7%8BJSON%E6%96%87%E6%9C%AC%E6%A0%BC%E5%BC%8F
            event.disconnect.accept(new ArrayBuilder()
                    .a("Hi ")
                    .a(event.username)
                    .a(", Your id is ")
                    .a(event.uuid)
                    .a(", Your login token is ")
                    .a(String.valueOf(EncryptionRequest.random.nextLong()))
                    .a(". Have a nice day.")
                    .toString());
        });
        /*
        PlayerListPingEvent.BUS.register(event -> {
            // https://wiki.vg/Server_List_Ping#Response
            event.response = new ObjectBuilder()
                    .a("version", new ObjectBuilder()
                            .a("name", "<Over 1.7>")
                            .a("protocol", event.protocol) // https://wiki.vg/Protocol_version_numbers
                            .build())
                    .a("players", new ObjectBuilder()
                            .a("max", Instant.now().atZone(ZoneId.systemDefault()).getYear())
                            .a("online", 114514)
                            .a("sample", new ArrayBuilder().b())
                            .build())
                    .a("description", Descriptions.random())
                    .toString();
        });
        */
        startup(PORT);
    }

    public static void startup(int port) {
        new ServerBootstrap().channel(NioServerSocketChannel.class)
                .group(new NioEventLoopGroup(70))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.attr(co).set(new ChannelOptionImpl(ch));
                        ch.pipeline()
                                .addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                .addLast("encoder", new MinecraftPacketMessageEncoder())
                                .addLast("decoder", new MinecraftPacketMessageDecoder())
                                .addLast(new FakeServer());
                    }
                }).bind(port)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("Server start on " + port);
                    } else {
                        future.cause().printStackTrace();
                        if (EXIT)
                            System.exit(1);
                    }
                });
    }

    private static class ChannelOptionImpl implements ChannelOptions {
        private final Channel channel;
        public static final HandshakingHandler handshaking = new HandshakingHandler();
        private MessageHandler handler = handshaking;

        public ChannelOptionImpl(Channel ch) {
            this.channel = ch;
        }

        @Override
        public void setListener(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public MessageHandler getListener() {
            return handler;
        }

        @Override
        public void disconnect() {
            channel.close();
        }

        @Override
        public boolean isOpen() {
            return channel.isOpen();
        }

        @Override
        public void write(Object message) {
            channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (DEBUG)
            cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final ChannelOptions options = ctx.channel().attr(co).get();
            final MessageHandler listener = options.getListener();
            if (listener != null) {
                listener.handle(ctx, PacketDataSerializer.fromByteBuf((ByteBuf) msg), options);
            }
        }
    }
}
