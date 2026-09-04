package tech.hakuri.teto.hook;

import tech.hakuri.teto.event.impl.EventCamera;
import tech.hakuri.teto.utils.ReflectBridge;
import org.objectweb.asm.*;

// EventCameraProvider实现
public class CameraHook {
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
            if ("m_90572_".equals(name) || "setRotation".equals(name)) {
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

            mv.visitVarInsn(Opcodes.FLOAD, 1);
            mv.visitVarInsn(Opcodes.FLOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventCamera.class), ReflectBridge.firstMethodName(EventCamera.class), Type.getMethodDescriptor(Type.getType(float[].class), Type.FLOAT_TYPE, Type.FLOAT_TYPE), false);
            // 此时栈顶已经存在一个float[]数组
            // 复制栈顶的数组引用，因为后续操作会消耗栈顶元素
            mv.visitInsn(Opcodes.DUP);

            // 加载常量0（数组索引）
            mv.visitInsn(Opcodes.ICONST_0);
            // 执行float[0]的读取操作
            mv.visitInsn(Opcodes.FALOAD);
            // 将读取到的float值存入本地变量表的槽位1
            // 我们刚刚取的就是槽位1
            mv.visitVarInsn(Opcodes.FSTORE, 1);

            // 此时栈顶仍然存在原始的数组引用
            // 加载常量1（数组索引）
            mv.visitInsn(Opcodes.ICONST_1);
            // 执行float[1]的读取操作
            mv.visitInsn(Opcodes.FALOAD);
            // 将读取到的float值存入本地变量表的槽位2
            // 我们刚刚也取了槽位2
            mv.visitVarInsn(Opcodes.FSTORE, 2);
        }
    }
}