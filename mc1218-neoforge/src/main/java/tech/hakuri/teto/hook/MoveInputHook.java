package tech.hakuri.teto.hook;

import tech.hakuri.teto.event.impl.EventMoveInput;
import tech.hakuri.teto.utils.ReflectBridge;
import org.objectweb.asm.*;

/**
 * 1.21.8 版本专属：注入点与 1.20.1 那份完全不同。
 * <p>
 * 1.20.1 的 {@code KeyboardInput.tick(boolean, float)} 里有一个 {@code IFEQ}
 * （潜行时缩放输入的那个判断），原实现就挂在那上面。
 * 但 1.21.8 的 {@code tick()} 被重写成了<b>无分支</b>的直线代码：
 * <pre>
 *   keyPresses = new Input(上,下,左,右,跳,潜行,疾跑)
 *   f1 = calculateImpulse(forward, backward)
 *   f2 = calculateImpulse(left, right)
 *   moveVector = new Vec2(f2, f1).normalized()
 *   return
 * </pre>
 * 分支都进了 {@code calculateImpulse} 这个静态方法里。所以按 {@code IFEQ} 注入
 * <b>一条指令都插不进去</b> —— 表现就是 RotationManager 的移动补偿完全不生效，
 * 搭路时人往假朝向走，桥会歪成阶梯状。
 * <p>
 * 改为在 {@code RETURN} 之前注入：此时 {@code moveVector} 已经算好并写入字段，
 * 事件消费方（RotationManager）读到的是完整的本帧输入，改写也不会被后面的代码覆盖。
 */
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
            //1.21.8 的 tick 是无参的；限定描述符避免误伤将来可能出现的其他重载
            if ("tick".equals(name) && "()V".equals(descriptor)) {
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
            if (opcode == Opcodes.RETURN) {
                //moveVector 此刻已经写好，把 this 交给事件，消费方可读可改
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventMoveInput.class),
                        ReflectBridge.firstMethodName(EventMoveInput.class),
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
            }
            super.visitInsn(opcode);
        }
    }
}
