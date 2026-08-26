package client.hook;

import client.event.impl.EventInvMove;
import client.utils.ReflectBridge;
import net.minecraft.client.KeyMapping;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LabelNode;

public class InvMoveHook {
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
            if ("m_90857_".equals(name) || "isDown".equals(name)) {
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

            mv.visitVarInsn(Opcodes.ALOAD, 0);//直接this压进去，等下反射拿值
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventInvMove.class), ReflectBridge.firstMethodName(EventInvMove.class), Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(KeyMapping.class)), false);
            mv.visitInsn(Opcodes.DUP);//一个返回值同时兼顾钩子开关和修改结果，要注意下
            LabelNode cancel = new LabelNode();
            mv.visitJumpInsn(Opcodes.IFEQ, cancel.getLabel());
            mv.visitInsn(Opcodes.IRETURN);//boolean就是int
            mv.visitLabel(cancel.getLabel());

        }
    }
}