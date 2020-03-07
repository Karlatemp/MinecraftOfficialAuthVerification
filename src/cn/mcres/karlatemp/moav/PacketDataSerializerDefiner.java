/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/07 12:37:51
 *
 * MinecraftOfficialAuthVerification/MinecraftOfficialAuthVerification/PacketDataSerializerDefiner.java
 */

package cn.mcres.karlatemp.moav;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class PacketDataSerializerDefiner {
    public static ClassNode load() {
        ClassNode node = new ClassNode();
        node.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "cn/mcres/karlatemp/internal/" + UUID.randomUUID() + "/PacketDataSerializer",
                null, "cn/mcres/karlatemp/moav/PacketDataSerializer", null
        );
        final MethodVisitor init = node.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lio/netty/buffer/ByteBuf;)V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ALOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "cn/mcres/karlatemp/moav/PacketDataSerializer", "<init>", "(Lio/netty/buffer/ByteBuf;)V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(2, 2);
        for (Method met : PacketDataSerializer.class.getMethods()) {
            if (Modifier.isAbstract(met.getModifiers())) {
                final MethodVisitor visitor = node.visitMethod(Opcodes.ACC_PUBLIC, met.getName(), Type.getMethodDescriptor(met), null, null);
                visitor.visitVarInsn(Opcodes.ALOAD, 0);
                visitor.visitFieldInsn(Opcodes.GETFIELD, "cn/mcres/karlatemp/moav/PacketDataSerializer", "a", "Lio/netty/buffer/ByteBuf;");
                final Type[] args = Type.getArgumentTypes(met);
                int stacks = 1;
                for (Type arg : args) {
                    visitor.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), stacks);
                    stacks += arg.getSize();
                }
                boolean inf = met.getDeclaringClass().isInterface();
                visitor.visitMethodInsn(
                        inf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                        Type.getType(met.getDeclaringClass()).getInternalName(),
                        met.getName(),
                        Type.getMethodDescriptor(met), inf);
                visitor.visitInsn(Type.getReturnType(met).getOpcode(Opcodes.IRETURN));
                visitor.visitMaxs(stacks + 1, stacks);
            }
        }
        return node;
    }
}
