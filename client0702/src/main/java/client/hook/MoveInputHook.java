package client.hook;

import client.event.impl.EventMoveInput;
import client.utils.ReflectBridge;
import net.minecraft.client.player.KeyboardInput;
import org.objectweb.asm.*;

//氯雷他定
public class MoveInputHook {
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
            if ("m_214106_".equals(name) || "tick".equals(name)) {
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
        public void visitJumpInsn(int opcode, Label label) {

            if (opcode == Opcodes.IFEQ) {//if(true)这样子的判断基本都是IFEQ，意思是if equals
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventMoveInput.class), ReflectBridge.firstMethodName(EventMoveInput.class), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(KeyboardInput.class)), false);
            }

            super.visitJumpInsn(opcode, label);
        }
    }
}