package tech.hakuri.teto.hook;

import tech.hakuri.teto.event.impl.EventRender3D;
import tech.hakuri.teto.utils.ReflectBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import org.objectweb.asm.*;

/**
 * 1.21.8 版本专属。
 * <p>
 * 1.20.1 挂的是 {@code GameRenderer.renderItemInHand(PoseStack, Camera, float)}，
 * {@code ALOAD 1} 就是世界变换栈。1.21.8 这个方法的签名变成了
 * {@code renderItemInHand(float, boolean, Matrix4f)} —— <b>槽 1 是 float，不再是 PoseStack</b>，
 * 照搬会直接 VerifyError，而且方法里根本没有 PoseStack 可拿。
 * <p>
 * 改挂 {@code LevelRenderer.renderEntities(PoseStack, BufferSource, Camera, DeltaTracker, List)}：
 * 槽 1 就是带相机变换的世界 PoseStack，而且它正好在实体渲染阶段被调用，
 * 对 ESP 这类世界空间描边来说时机比 renderItemInHand 更合适。
 */
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
            //只认第一个参数是 PoseStack 的那个重载，避免误伤同名方法
            if ("renderEntities".equals(name) && descriptor.startsWith("(Lcom/mojang/blaze3d/vertex/PoseStack;")) {
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
            mv.visitVarInsn(Opcodes.ALOAD, 1);//1参是世界 PoseStack
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(EventRender3D.class), ReflectBridge.firstMethodName(EventRender3D.class), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(PoseStack.class)), false);
        }
    }
}
