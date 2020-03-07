/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 15:50:33
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/LoginSuccessEvent.java
 */

package cn.mcres.karlatemp.moav.event;

import cn.mcres.karlatemp.moav.ChannelOptions;
import cn.mcres.karlatemp.moav.util.EventBus;

import java.util.function.Consumer;

public class LoginSuccessEvent {
    public String username;
    public String uuid;
    public ChannelOptions options;
    public boolean disconnected;
    // @see https://minecraft.gamepedia.com/Raw_JSON_text_format
    // @see https://minecraft-zh.gamepedia.com/%E5%8E%9F%E5%A7%8BJSON%E6%96%87%E6%9C%AC%E6%A0%BC%E5%BC%8F
    public Consumer<String> disconnect;
    public static final EventBus<LoginSuccessEvent> BUS = new EventBus<>();
}
