/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:27:46
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/ObjectBuilder.java
 */

package cn.mcres.karlatemp.moav.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ObjectBuilder {
    private final JsonObject obj;

    public ObjectBuilder() {
        this.obj = new JsonObject();
    }

    public ObjectBuilder a(String k, Number val) {
        obj.addProperty(k, val);
        return this;
    }

    public ObjectBuilder a(String k, String val) {
        obj.addProperty(k, val);
        return this;
    }

    public ObjectBuilder a(String k, boolean val) {
        obj.addProperty(k, val);
        return this;
    }

    public ObjectBuilder a(String k, JsonElement val) {
        obj.add(k, val);
        return this;
    }

    public JsonObject build() {
        return obj;
    }

    public String toString() {
        return obj.toString();
    }
}
