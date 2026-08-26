package client.hook;

import client.event.impl.EventChamsAfter;
import client.event.impl.EventChamsPre;
import client.utils.ReflectBridge;
import org.objectweb.asm.*;

public class ChamsHook {
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
            if ("m_114384_".equals(name) || "render".equals(name)) {
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
            // 方法头部插入
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventChamsPre.class), ReflectBridge.firstMethodName(EventChamsPre.class), Type.getMethodDescriptor(Type.VOID_TYPE), false);
        }

        @Override
        public void visitInsn(int opcode) {
            // 方法尾部（任意一个返回）前插入
            if (opcode == Opcodes.RETURN) {//注意了我们要hook的方法是void的所以指令是RETURN，如果是返回其他类型，请按鼠标中键点一下RETURN
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventChamsAfter.class), ReflectBridge.firstMethodName(EventChamsAfter.class), Type.getMethodDescriptor(Type.VOID_TYPE), false);
            }
            super.visitInsn(opcode);//visitInsn的super()似乎都是在最后调用
        }
    }
}
