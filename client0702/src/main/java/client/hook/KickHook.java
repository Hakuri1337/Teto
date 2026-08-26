package client.hook;

import client.event.impl.EventKick;
import client.utils.ReflectBridge;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LabelNode;


public class KickHook {
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

            if ("m_7026_".equals(name) || "onDisconnect".equals(name)) {
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

            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventKick.class), ReflectBridge.firstMethodName(EventKick.class), Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Component.class)), false);
            LabelNode cancel = new LabelNode();
            mv.visitJumpInsn(Opcodes.IFEQ, cancel.getLabel());
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(cancel.getLabel());
        }
    }
}