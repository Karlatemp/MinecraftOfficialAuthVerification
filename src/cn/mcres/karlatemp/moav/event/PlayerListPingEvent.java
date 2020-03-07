/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 16:16:34
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/PlayerListPingEvent.java
 */

package cn.mcres.karlatemp.moav.event;

import cn.mcres.karlatemp.moav.util.EventBus;

public class PlayerListPingEvent {
    // @see https://wiki.vg/Server_List_Ping#Response
    public String response;
    public String host;
    public int port;
    public int protocol;
    public static final EventBus<PlayerListPingEvent> BUS = new EventBus<>();
}
