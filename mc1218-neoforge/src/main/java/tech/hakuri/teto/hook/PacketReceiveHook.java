package tech.hakuri.teto.hook;

import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.network.protocol.Packet;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LabelNode;


public class PacketReceiveHook {
    public static byte[] transform(byte[] basicClass) {
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new CV(Opcodes.ASM9, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    static class CV extends ClassVisitor {
        public CV(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            //不要插channelRead0，会双倍事件，插genericsFtw
            if ("m_129517_".equals(name) || "genericsFtw".equals(name)) {//genericsFtw m_129517_
                return new MV(api, mv);
            }

            return mv;
        }
    }

    static class MV extends MethodVisitor {
        public MV(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            mv.visitVarInsn(Opcodes.ALOAD, 0);//注意static
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventPacket.class), ReflectBridge.firstMethodName(EventPacket.class), Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Packet.class)), false);
            LabelNode cancel = new LabelNode();
            mv.visitJumpInsn(Opcodes.IFEQ, cancel.getLabel());
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(cancel.getLabel());

        }
    }
}