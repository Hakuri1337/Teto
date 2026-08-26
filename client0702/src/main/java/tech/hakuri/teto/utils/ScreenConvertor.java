package tech.hakuri.teto.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;

/**
 * @author 豆包
 * nametag要用的，3d映射2d
 */
public class ScreenConvertor {
    public static Minecraft mc = Minecraft.getInstance();

    public static Vec2 toScreen(Vec3 worldPos) {
        // 获取相机信息
        var cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        var cameraForward = mc.gameRenderer.getMainCamera().getLookVector();
        var cameraUp = new Vec3(0, 1, 0); // 世界向上方向

        // 构建视图矩阵
        var viewMatrix = createViewMatrix(cameraPos, cameraForward, cameraUp);

        // 获取投影矩阵
        var projectionMatrix = mc.gameRenderer.getProjectionMatrix(mc.getFrameTime());

        // 计算相对于相机的坐标
        var relativePos = worldPos.subtract(cameraPos);

        // 应用视图矩阵
        var viewPos = applyMatrix(new Vector4d(relativePos.x, relativePos.y, relativePos.z, 1.0f), viewMatrix);

        // 应用投影矩阵
        var clipPos = applyMatrix(viewPos, projectionMatrix);

        // 透视除法：转换为NDC坐标
        if (clipPos.w == 0) return null;

        var ndcX = clipPos.x / clipPos.w;
        var ndcY = clipPos.y / clipPos.w;
        var ndcZ = clipPos.z / clipPos.w;

        // 检查是否在可见范围内
        if (Math.abs(ndcX) > 1 || Math.abs(ndcY) > 1 || Math.abs(ndcZ) > 1) {
            return null;
        }

        // 转换为屏幕坐标（原点在左上角）
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int screenX = (int) ((ndcX + 1) / 2 * screenWidth);
        int screenY = (int) ((1 - ndcY) / 2 * screenHeight);

        return new Vec2(screenX, screenY);
    }

    public static Matrix4d createViewMatrix(Vec3 cameraPos, Vector3f cameraForward, Vec3 cameraUp) {
        // 使用JOML的lookAt方法构建视图矩阵
        return new Matrix4d().lookAt(
                cameraPos.x, cameraPos.y, cameraPos.z,
                cameraPos.x + cameraForward.x, cameraPos.y + cameraForward.y, cameraPos.z + cameraForward.z,
                cameraUp.x, cameraUp.y, cameraUp.z
        );
    }

    public static Vector4d applyMatrix(Vector4d vec, Matrix4d mat) {
        return new Vector4d(
                vec.x * mat.m00() + vec.y * mat.m10() + vec.z * mat.m20() + vec.w * mat.m30(),
                vec.x * mat.m01() + vec.y * mat.m11() + vec.z * mat.m21() + vec.w * mat.m31(),
                vec.x * mat.m02() + vec.y * mat.m12() + vec.z * mat.m22() + vec.w * mat.m32(),
                vec.x * mat.m03() + vec.y * mat.m13() + vec.z * mat.m23() + vec.w * mat.m33()
        );
    }

    public static Vector4d applyMatrix(Vector4d vec, Matrix4f mat) {
        return new Vector4d(
                vec.x * mat.m00() + vec.y * mat.m10() + vec.z * mat.m20() + vec.w * mat.m30(),
                vec.x * mat.m01() + vec.y * mat.m11() + vec.z * mat.m21() + vec.w * mat.m31(),
                vec.x * mat.m02() + vec.y * mat.m12() + vec.z * mat.m22() + vec.w * mat.m32(),
                vec.x * mat.m03() + vec.y * mat.m13() + vec.z * mat.m23() + vec.w * mat.m33()
        );
    }

}