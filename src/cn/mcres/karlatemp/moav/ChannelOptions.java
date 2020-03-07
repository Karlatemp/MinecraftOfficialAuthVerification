/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:16:33
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/ChannelOptions.java
 */

package cn.mcres.karlatemp.moav;

public interface ChannelOptions {
    void setListener(MessageHandler handler);

    MessageHandler getListener();

    void disconnect();

    void write(Object message);

    boolean isOpen();
}
