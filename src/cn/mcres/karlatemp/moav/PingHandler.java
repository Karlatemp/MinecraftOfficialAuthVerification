/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:22:25
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/PingHandler.java
 */

package cn.mcres.karlatemp.moav;

import cn.mcres.karlatemp.moav.event.PlayerListPingEvent;
import cn.mcres.karlatemp.moav.util.ArrayBuilder;
import cn.mcres.karlatemp.moav.util.ObjectBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

public class PingHandler implements MessageHandler {


    public static ByteBuf response(Channel channel) {
        final PlayerListPingEvent event = new PlayerListPingEvent();
        event.host = channel.attr(HandshakingHandler.CHANNEL_SERVER_HOST).get();
        event.port = channel.attr(HandshakingHandler.CHANNEL_SERVER_PORT).get();
        event.protocol = channel.attr(HandshakingHandler.CHANNEL_PROTOCOL_VERSION).get();
        String response = PlayerListPingEvent.BUS.call(event).response;
        if (response == null) {
            response = defaultResponse(event);
        }
        byte[] message = response.getBytes(StandardCharsets.UTF_8);
        int size = message.length + PacketDataSerializer.varIntLength(0) + PacketDataSerializer.varIntLength(message.length);
        final PacketDataSerializer buf = PacketDataSerializer.fromByteBuf(Unpooled.directBuffer(size));
        buf.writeVarInt(0);
        buf.writeVarInt(message.length);
        buf.writeBytes(message);
        return buf;
    }


    private static String defaultResponse(PlayerListPingEvent event) {
        // https://wiki.vg/Server_List_Ping#Response
        return new ObjectBuilder()
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
    }

    private boolean step;

    @Override
    public void handle(ChannelHandlerContext context, PacketDataSerializer message, ChannelOptions options) {
        final int i = message.readVarInt();
        if (step) {
            if (i == 1) {
                final PacketDataSerializer buffer = PacketDataSerializer.fromByteBuf(Unpooled.buffer(PacketDataSerializer.varIntLength(0) + Long.BYTES));
                buffer.writeVarInt(1);
                buffer.writeLong(message.readLong());
                options.write(buffer);
            }
            options.disconnect();
        } else {
            if (i != 0) {
                options.disconnect();
                return;
            }
            options.write(response(context.channel()));
            step = true;
        }
    }
}
