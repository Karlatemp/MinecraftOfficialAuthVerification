/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 14:11:44
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/EncryptionRequest.java
 */

package cn.mcres.karlatemp.moav.util;

import cn.mcres.karlatemp.moav.PacketDataSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptionRequest {
    public static final SecureRandom random = new SecureRandom();
    public static final KeyPair keys;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keys = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public final String hash;
    public final byte[] pubKey;
    public final byte[] verify;

    public EncryptionRequest(String hash, byte[] pubKey, byte[] verify) {
        this.hash = hash;
        this.pubKey = pubKey;
        this.verify = verify;
    }

    public static EncryptionRequest request() {
        String hash = Long.toString(random.nextLong(), 16);
        byte[] pubKey = keys.getPublic().getEncoded();
        byte[] verify = new byte[4];
        random.nextBytes(verify);
        return new EncryptionRequest(hash, pubKey, verify);
    }

    public ByteBuf compile() {
        byte[] id = hash.getBytes(StandardCharsets.UTF_8);
        int size = a(id) + a(pubKey) + a(verify) + PacketDataSerializer.varIntLength(1);
        PacketDataSerializer serializer = PacketDataSerializer.fromByteBuf(Unpooled.buffer(size));
        serializer.writeVarInt(1);
        serializer.writeVarInt(id.length);
        serializer.writeBytes(id);
        serializer.writeVarInt(pubKey.length);
        serializer.writeBytes(pubKey);
        serializer.writeVarInt(verify.length);
        serializer.writeBytes(verify);
        return serializer;
    }

    private static int a(byte[] verify) {
        return PacketDataSerializer.varIntLength(verify.length) + verify.length;
    }
}
