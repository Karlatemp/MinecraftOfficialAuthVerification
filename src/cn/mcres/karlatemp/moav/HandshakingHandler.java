/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:15:56
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/HandshakingHandler.java
 */

package cn.mcres.karlatemp.moav;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class HandshakingHandler implements MessageHandler {
    public static final AttributeKey<Integer> CHANNEL_PROTOCOL_VERSION =
            AttributeKey.valueOf("Karlatemp-MOAV-Protocol-Ver");
    public static final AttributeKey<Integer> CHANNEL_SERVER_PORT =
            AttributeKey.valueOf("Karlatemp-MOAV-Address-Port");
    public static final AttributeKey<String> CHANNEL_SERVER_HOST =
            AttributeKey.valueOf("Karlatemp-MOAV-Address-Host");

    @Override
    public void handle(ChannelHandlerContext context,
                       PacketDataSerializer message,
                       ChannelOptions options) {
        final int id = message.readVarInt();
        if (id != 0) {
            options.disconnect();
        } else {
            int protocol = message.readVarInt();
            context.channel().attr(CHANNEL_PROTOCOL_VERSION).set(protocol);
            context.channel().attr(CHANNEL_SERVER_HOST).set(message.readString(255));
            context.channel().attr(CHANNEL_SERVER_PORT).set(message.readUnsignedShort());
            int next = message.readVarInt();
            switch (next) {
                case 1: {
                    options.setListener(new PingHandler());
                    break;
                }
                case 2: {
                    options.setListener(new LoginHandler());
                    break;
                }
                default:
                    options.disconnect();
            }
        }
    }
}