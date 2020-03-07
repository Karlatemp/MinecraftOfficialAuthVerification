package cn.mcres.karlatemp.moav;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.UUID;

/**
 * Create at 2020/3/7 12:27
 * Copyright Karlatemp
 * MinecraftOfficialAuthVerification $ cn.mcres.karlatemp.moav
 */
public abstract class PacketDataSerializer extends ByteBuf {
    public static final MethodHandle ALLOCATE;

    static {
        final ClassNode node = PacketDataSerializerDefiner.load();
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Class<? extends PacketDataSerializer> implement = new ClassLoader(PacketDataSerializer.class.getClassLoader()) {
            Class<? extends PacketDataSerializer> load(byte[] code) {
                Permissions ps = new Permissions();
                ps.add(new AllPermission());
                ps.setReadOnly();
                ProtectionDomain pd = new ProtectionDomain(
                        PacketDataSerializer.class.getProtectionDomain().getCodeSource(), ps
                );
                return defineClass(null, code, 0, code.length, pd).asSubclass(PacketDataSerializer.class);
            }
        }.load(writer.toByteArray());
        try {
            ALLOCATE = MethodHandles.lookup().unreflectConstructor(implement.getConstructor(ByteBuf.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    @Contract("null -> null; !null -> !null")
    public static PacketDataSerializer fromByteBuf(ByteBuf buf) {
        if (buf == null) return null;
        if (buf instanceof PacketDataSerializer) return (PacketDataSerializer) buf;
        try {
            return (PacketDataSerializer) ALLOCATE.invoke(buf);
        } catch (Throwable throwable) {
            throw new InternalError(throwable);
        }
    }

    protected final ByteBuf a;

    public final ByteBuf buffer() {
        return a;
    }

    protected PacketDataSerializer(ByteBuf bytebuf) {
        this.a = bytebuf;
    }

    public static int varIntLength(int i) {
        for (int j = 1; j < 5; ++j) {
            if ((i & -1 << j * 7) == 0) {
                return j;
            }
        }

        return 5;
    }

    public PacketDataSerializer writeByteArray(byte[] abyte) {
        writeVarInt(abyte.length);
        a.writeBytes(abyte);
        return this;
    }

    public byte[] readByteArray() {
        return this.readByteArray(a.readableBytes());
    }

    public byte[] readByteArray(int allows) {
        int j = this.readVarInt();
        if (j > allows) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + allows);
        } else {
            byte[] abyte = new byte[j];
            a.readBytes(abyte);
            return abyte;
        }
    }

    public PacketDataSerializer writeVarIntArray(int[] aint) {
        this.writeVarInt(aint.length);
        int[] aint1 = aint;
        int i = aint.length;

        for (int j = 0; j < i; ++j) {
            int k = aint1[j];

            this.writeVarInt(k);
        }

        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(a.readableBytes());
    }

    public int[] readVarIntArray(int allowed) {
        int j = this.readVarInt();

        if (j > allowed) {
            throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + allowed);
        } else {
            int[] aint = new int[j];

            for (int k = 0; k < aint.length; ++k) {
                aint[k] = this.readVarInt();
            }

            return aint;
        }
    }

    public PacketDataSerializer writeLongArray(long[] along) {
        this.writeVarInt(along.length);
        long[] along1 = along;
        int i = along.length;

        for (int j = 0; j < i; ++j) {
            long k = along1[j];
            a.writeLong(k);
        }

        return this;
    }

    public <T extends Enum<T>> T readEnum(Class<T> oclass) {
        return (oclass.getEnumConstants())[this.readVarInt()];
    }

    public PacketDataSerializer writeEnum(Enum<?> oenum) {
        return this.writeVarInt(oenum.ordinal());
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;

        byte b0;

        do {
            b0 = a.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public long readVarLong() {
        long i = 0L;
        int j = 0;

        byte b0;

        do {
            b0 = a.readByte();
            i |= (long) (b0 & 127) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public PacketDataSerializer writeUUID(UUID uuid) {
        a.writeLong(uuid.getMostSignificantBits());
        a.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(a.readLong(), a.readLong());
    }

    public PacketDataSerializer writeVarInt(int i) {
        while ((i & -128) != 0) {
            a.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        a.writeByte(i);
        return this;
    }

    public PacketDataSerializer writeVarLong(long i) {
        while ((i & -128L) != 0L) {
            a.writeByte((int) (i & 127L) | 128);
            i >>>= 7;
        }

        a.writeByte((int) i);
        return this;
    }

    public String readString() {
        return readString(32767);
    }

    public String readString(int i) {
        int j = this.readVarInt();

        if (j > i * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i * 4 + ")");
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(a.readerIndex(), j, StandardCharsets.UTF_8);

            a.readerIndex(a.readerIndex() + j);
            if (s.length() > i) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + i + ")");
            } else {
                return s;
            }
        }
    }

    public PacketDataSerializer writeString(String s) {
        return this.writeString(s, 32767);
    }

    public PacketDataSerializer writeString(String s, int allowed) {
        byte[] abyte = s.getBytes(StandardCharsets.UTF_8);

        if (abyte.length > allowed) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + allowed + ")");
        } else {
            this.writeVarInt(abyte.length);
            a.writeBytes(abyte);
            return this;
        }
    }

    public Date readDate() {
        return new Date(a.readLong());
    }

    public PacketDataSerializer writeDate(Date date) {
        a.writeLong(date.getTime());
        return this;
    }

    @Override
    public String toString() {
        return a.toString();
    }

    public String toString(Charset charset) {
        return a.toString(charset);
    }

    public String toString(int index, int length, Charset charset) {
        return a.toString(index, length, charset);
    }
}
