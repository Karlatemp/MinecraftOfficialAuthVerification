/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 15:29:54
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/LoginResult.java
 */

package cn.mcres.karlatemp.moav.util;

public class LoginResult {
    public String id;
    public String name;
    public Property[] properties;

    public static class Property {

        public String name;
        public String value;
        public String signature;
    }
}
