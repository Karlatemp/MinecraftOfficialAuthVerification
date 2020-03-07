/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:29:16
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/ArrayBuilder.java
 */

package cn.mcres.karlatemp.moav.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ArrayBuilder {
    private final JsonArray obj;

    public ArrayBuilder() {
        this.obj = new JsonArray();
    }

    public ArrayBuilder a(Number v) {
        obj.add(v);
        return this;
    }

    public ArrayBuilder a(boolean v) {
        obj.add(v);
        return this;
    }

    public ArrayBuilder a(String v) {
        obj.add(v);
        return this;
    }

    public ArrayBuilder a(JsonElement v) {
        obj.add(v);
        return this;
    }

    public JsonArray b() {
        return obj;
    }

    public String toString() {
        return obj.toString();
    }
}
