/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 16:26:39
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/Descriptions.java
 */

package cn.mcres.karlatemp.moav;

import cn.mcres.karlatemp.moav.util.EncryptionRequest;
import cn.mcres.karlatemp.moav.util.ObjectBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Descriptions {
    public static final List<JsonElement> descriptions = new ArrayList<>();

    public static JsonElement random() {
        return descriptions.get(
                (int) (EncryptionRequest.random.nextDouble() * descriptions.size())
        );
    }

    static {
        final InputStream stream = Descriptions.class.getResourceAsStream("descriptions.json");
        if (stream == null) {
            descriptions.add(new ObjectBuilder().a("text", "descriptions missing").build());
        } else {
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                final JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement element : array) {
                    descriptions.add(element);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                descriptions.add(new ObjectBuilder().a("text", "descriptions missing").build());
            }
        }
    }
}
