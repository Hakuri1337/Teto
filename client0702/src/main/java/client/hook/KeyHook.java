package client.hook;

import client.event.impl.EventKey;
import client.utils.ReflectBridge;
import org.objectweb.asm.*;


public class KeyHook {
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
            if ("m_90893_".equals(name) || "keyPress".equals(name)) {
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
            mv.visitVarInsn(Opcodes.ILOAD, 3);//注意了，long和double都是占两个局部变量表空间，非static方法局部变量表0号固定为this（这个是），如果是static方法则没有this，1参从0号开始
            mv.visitVarInsn(Opcodes.ILOAD, 4);//方法参数基本都是按顺序从头放在局部变量表的，只要方法参数不变，这段hook应该也挺稳定
            mv.visitVarInsn(Opcodes.ILOAD, 5);
            mv.visitVarInsn(Opcodes.ILOAD, 6);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventKey.class), ReflectBridge.firstMethodName(EventKey.class), Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE), false);
        }
    }
}