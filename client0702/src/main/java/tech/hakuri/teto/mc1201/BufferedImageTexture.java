package tech.hakuri.teto.mc1201;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link tech.hakuri.teto.platform.IRenderer#image} 在 1.20.1 的纹理缓存。
 * 与 1.21.8 的 {@code BufferedImageTexture} 同一思路：BufferedImage → NativeImage
 * → DynamicTexture → 注册。1.20.1 的 ResourceLocation 构造器是公开的，这点比 1.21.8 简单。
 */
final class BufferedImageTexture {

    private static final Map<BufferedImage, ResourceLocation> CACHE = new HashMap<>();

    private BufferedImageTexture() {
    }

    static ResourceLocation of(BufferedImage image) {
        ResourceLocation cached = CACHE.get(image);
        if (cached != null) {
            return cached;
        }
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                nativeImage.setPixelRGBA(x, y, image.getRGB(x, y));
            }
        }
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation location = new ResourceLocation("teto", "ui/" + CACHE.size());
        Minecraft.getInstance().getTextureManager().register(location, texture);
        CACHE.put(image, location);
        return location;
    }

    static void clear() {
        for (ResourceLocation location : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        CACHE.clear();
    }
}
