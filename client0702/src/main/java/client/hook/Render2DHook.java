package client.hook;

import client.event.impl.EventRender2D;
import client.utils.ReflectBridge;
import org.objectweb.asm.*;

public class Render2DHook {
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
            if ("m_280130_".equals(name) || "renderCrosshair".equals(name)) {
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
        public void visitInsn(int opcode) {
            //在方法返回前插入事件
            if (opcode == Opcodes.RETURN) {//注意了我们要hook的方法是void的所以指令是RETURN，如果是返回其他类型，请按鼠标中键点一下RETURN
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventRender2D.class), ReflectBridge.firstMethodName(EventRender2D.class), Type.getMethodDescriptor(Type.VOID_TYPE), false);
            }

            super.visitInsn(opcode);//visitInsn的super()似乎都是在最后调用
        }
    }
}