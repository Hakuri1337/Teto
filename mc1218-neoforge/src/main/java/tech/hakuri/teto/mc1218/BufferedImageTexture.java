package tech.hakuri.teto.mc1218;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link tech.hakuri.teto.platform.IRenderer#image} 在 1.21.8 的纹理缓存。
 * <p>
 * 与 {@link AtlasFont} 同一条通路：BufferedImage → NativeImage → DynamicTexture
 * → TextureManager.register → blit。区别是这里缓存的是「UI 预烘纹理」
 * （圆角矩形/阴影/辉光的九宫格源图），按源图身份缓存，只上传一次，
 * 运行时只负责贴，不再重传。
 */
final class BufferedImageTexture {

    private static final Map<BufferedImage, ResourceLocation> CACHE = new HashMap<>();

    private BufferedImageTexture() {
    }

    /** 取（必要时上传）这张图对应的纹理位置。 */
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
        DynamicTexture texture = new DynamicTexture(() -> "teto-ui-" + CACHE.size(), nativeImage);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("teto", "ui/" + CACHE.size());
        Minecraft.getInstance().getTextureManager().register(location, texture);
        CACHE.put(image, location);
        return location;
    }

    /** 释放全部缓存的纹理（主题/资源重载时）。 */
    static void clear() {
        for (ResourceLocation location : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        CACHE.clear();
    }
}
