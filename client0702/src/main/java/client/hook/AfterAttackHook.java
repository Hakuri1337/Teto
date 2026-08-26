package client.hook;

import client.event.impl.EventAfterAttack;
import client.utils.ReflectBridge;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.*;

public class AfterAttackHook {
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
            if ("m_5706_".equals(name) || "attack".equals(name)) {
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
            if (opcode == Opcodes.RETURN) {//void方法，普通return
                mv.visitVarInsn(Opcodes.ALOAD, 1);//1参entity
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventAfterAttack.class), ReflectBridge.firstMethodName(EventAfterAttack.class), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Entity.class)), false);
            }

            super.visitInsn(opcode);//注意这个super的位置，若是往前挪，该功能会失效
        }
    }
}