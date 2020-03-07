/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 13:23:35
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/LoginHandler.java
 */

package cn.mcres.karlatemp.moav;

import cn.mcres.karlatemp.moav.event.LoginSuccessEvent;
import cn.mcres.karlatemp.moav.event.PreEncryptionEvent;
import cn.mcres.karlatemp.moav.util.EncryptionRequest;
import cn.mcres.karlatemp.moav.util.LoginResult;
import cn.mcres.karlatemp.moav.util.ObjectBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Arrays;

public class LoginHandler implements MessageHandler {
    public static final Gson gson = new Gson();
    public static final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
            .disableCookieManagement()
            .disableAuthCaching()
            .setUserAgent("Java/" + System.getProperty("java.version") + " HOAV/" + FakeServer.VERSION + " ApacheHttpClient/" + HttpAsyncClient.class.getPackage().getImplementationVersion())
            .build();

    static {
        client.start();
    }

    private EncryptionRequest request;

    public static void disconnect(ChannelOptions opts, String cause) {
        if (!opts.isOpen()) {
            return;
        }
        byte[] data = cause.getBytes(StandardCharsets.UTF_8);
        int len = PacketDataSerializer.varIntLength(0) + PacketDataSerializer.varIntLength(data.length) + data.length;
        final PacketDataSerializer serializer = PacketDataSerializer.fromByteBuf(Unpooled.buffer(len));
        serializer.writeVarInt(0);
        serializer.writeVarInt(data.length);
        serializer.writeBytes(data);
        opts.write(serializer);
        opts.disconnect();
    }

    public String username;
    public LoginStatus status = LoginStatus.WAIT_USERNAME;

    public enum LoginStatus {
        WAIT_USERNAME, WAIT_ENCRYPTION, ENCRYPTING, DONE;
    }

    @Override
    public void handle(ChannelHandlerContext context, PacketDataSerializer message, ChannelOptions options) {
        try {
            int id = message.readVarInt();
            switch (id) {
                case 0: {
                    if (status != LoginStatus.WAIT_USERNAME) {
                        disconnect(options, "{\"text\":\"Bad Packet.\"}");
                        return;
                    }
                    username = message.readString(16);
                    status = LoginStatus.WAIT_ENCRYPTION;
                    {
                        PreEncryptionEvent event = new PreEncryptionEvent();
                        event.context = context;
                        event.username = username;
                        PreEncryptionEvent.BUS.call(event);
                        if (event.disconnectMessage != null) {
                            disconnect(options, event.disconnectMessage);
                            return;
                        }
                    }
                    request = EncryptionRequest.request();
                    ByteBuf buf = request.compile();
                    options.write(buf);
                    break;
                }
                case 1: {
                    if (status != LoginStatus.WAIT_ENCRYPTION) {
                        disconnect(options, "{\"text\":\"Bad Packet.\"}");
                        return;
                    }
                    byte[] secret = message.readByteArray();
                    byte[] token = message.readByteArray();
                    SecretKey key;
                    {
                        final PrivateKey pri = EncryptionRequest.keys.getPrivate();
                        Cipher var3 = Cipher.getInstance(pri.getAlgorithm());
                        var3.init(Cipher.DECRYPT_MODE, pri);
                        key = new SecretKeySpec(var3.doFinal(secret), "AES");
                    }
                    {
                        Cipher decrypt = Cipher.getInstance("AES/CFB8/NoPadding");
                        decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));

                        Cipher encrypt = Cipher.getInstance("AES/CFB8/NoPadding");
                        encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
                        context.channel().pipeline().addBefore("decoder", "decrypt", new ChannelInboundHandlerAdapter() {
                            private byte[] b;

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof ByteBuf) {
                                    ByteBuf var1 = (ByteBuf) msg;
                                    int var2 = var1.readableBytes();
                                    byte[] var3 = this.a(var1);
                                    ByteBuf var4 = context.alloc().heapBuffer(decrypt.getOutputSize(var2));
                                    var4.writerIndex(decrypt.update(var3, 0, var2, var4.array(), var4.arrayOffset()));
                                    var1.release();
                                    ctx.fireChannelRead(var4);
                                }
                            }

                            private byte[] a(ByteBuf var0) {
                                int var1 = var0.readableBytes();
                                if (this.b.length < var1) {
                                    this.b = new byte[var1];
                                }
                                var0.readBytes(this.b, 0, var1);
                                return this.b;
                            }
                        });
                        context.channel().pipeline().addBefore("encoder", "encrypt", new ChannelOutboundHandlerAdapter() {
                            private byte[] b;
                            private byte[] c;

                            private byte[] a(ByteBuf var0) {
                                int var1 = var0.readableBytes();
                                if (this.b == null || this.b.length < var1) {
                                    this.b = new byte[var1];
                                }

                                var0.readBytes(this.b, 0, var1);
                                return this.b;
                            }

                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                if (msg instanceof ByteBuf) {
                                    ByteBuf var0 = (ByteBuf) msg;
                                    int var2 = var0.readableBytes();
                                    byte[] var3 = this.a(var0);
                                    int var4 = encrypt.getOutputSize(var2);
                                    if (this.c == null || this.c.length < var4) {
                                        this.c = new byte[var4];
                                    }
                                    final ByteBuf buffer = ctx.channel().alloc().buffer(var4);
                                    buffer.writeBytes(this.c, 0, encrypt.update(var3, 0, var2, this.c));
                                    var0.release();
                                    msg = buffer;
                                }
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                    /*if (!Arrays.equals(token, request.verify)) {
                        disconnect(options, "{\"text\":\"Invalid Token.\",\"color\":\"red\"}");
                        return;
                    }*/
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    for (byte[] bit : new byte[][]{
                            request.hash.getBytes("ISO_8859_1"), key.getEncoded(), EncryptionRequest.keys.getPublic().getEncoded()
                    }) {
                        sha.update(bit);
                    }
                    String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");
                    String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + username + "&serverId=" + encodedHash;
                    client.execute(SimpleHttpRequest.copy(new HttpGet(authURL)), new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(SimpleHttpResponse simpleHttpResponse) {
                            final Charset charset = simpleHttpResponse.getContentType().getCharset();
                            try (InputStreamReader reader = new InputStreamReader(
                                    new ByteArrayInputStream(simpleHttpResponse.getBodyBytes()),
                                    charset == null ? StandardCharsets.UTF_8 : charset
                            )) {
                                LoginResult obj = gson.fromJson(reader, LoginResult.class);
                                if (obj != null && obj.id != null) {
                                    final LoginSuccessEvent event = new LoginSuccessEvent();
                                    event.options = options;
                                    event.username = obj.name;
                                    event.uuid = obj.id;
                                    event.disconnected = false;
                                    event.disconnect = msg -> {
                                        if (event.disconnected) return;
                                        disconnect(options, msg);
                                        event.disconnected = true;
                                    };
                                    LoginSuccessEvent.BUS.call(event);
                                    if (!event.disconnected)
                                        disconnect(options, new ObjectBuilder().a("text", "Login with " + obj.id + "[" + obj.name + "]").toString());
                                    return;
                                }
                                disconnect(options, "{\"text\":\"Offline player.\"}");
                            } catch (Throwable ioe) {
                                disconnect(options, new ObjectBuilder().a("text", ioe.toString()).toString());
                            }
                        }

                        @Override
                        public void failed(Exception e) {
                            disconnect(options, new ObjectBuilder().a("text", e.toString()).toString());
                        }

                        @Override
                        public void cancelled() {
                            disconnect(options, "{\"text\":\"Verification Cancelled.\"}");
                        }
                    });
                }
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
