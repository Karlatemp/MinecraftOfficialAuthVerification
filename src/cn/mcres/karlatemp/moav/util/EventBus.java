/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 15:45:28
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/EventBus.java
 */

package cn.mcres.karlatemp.moav.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventBus<T> {
    public final ConcurrentLinkedQueue<EventHandler<T>> handlers = new ConcurrentLinkedQueue<>();

    public interface EventHandler<T> {
        void handle(T event);
    }

    public EventBus<T> register(EventHandler<T> handler) {
        handlers.add(handler);
        return this;
    }

    public T call(T event) {
        for (EventHandler<T> handler : handlers) {
            handler.handle(event);
        }
        return event;
    }
}
