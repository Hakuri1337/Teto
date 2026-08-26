package client.hook;

import client.event.impl.EventRender3D;
import client.utils.ReflectBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import org.objectweb.asm.*;

public class Render3DHook {
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
            if ("m_109120_".equals(name) || "renderItemInHand".equals(name)) {
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
            mv.visitVarInsn(Opcodes.ALOAD, 1);//拿1参
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventRender3D.class), ReflectBridge.firstMethodName(EventRender3D.class), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(PoseStack.class)), false);
        }
    }
}