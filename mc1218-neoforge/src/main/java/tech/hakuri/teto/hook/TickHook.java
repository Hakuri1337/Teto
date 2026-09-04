package tech.hakuri.teto.hook;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.utils.ReflectBridge;
import org.objectweb.asm.*;

public class TickHook {
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
            if ("m_8119_".equals(name) || "m_91398_".equals(name) || "tick".equals(name)) {
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
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventTick.class), ReflectBridge.firstMethodName(EventTick.class), Type.getMethodDescriptor(Type.VOID_TYPE), false);
        }
    }
}