/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:15:04
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/MessageHandler.java
 */

package cn.mcres.karlatemp.moav;

import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler {
    void handle(ChannelHandlerContext context,
                PacketDataSerializer message,
                ChannelOptions options);
}
