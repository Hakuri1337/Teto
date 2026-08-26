package client.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @author AquaVase, Hoshimi Miyabi ,手淫
 * 不管怎样，再次感谢氯雷他定
 * 由手淫完全重写并极限压缩200行
 * 可达到Doomsday同款渲染效果
 */
@Deprecated(since = "AquaVase & Hoshimi Miyabi & 氯雷他定 & 手淫")
public class TrueTypeFont implements AutoCloseable {
    public static int shadow = new Color(0, 0, 0, 128).getRGB();
    public Font font;
    public AffineTransform affineTransform;
    public FontMetrics fontMetrics;
    public FontRenderContext context;
    public HashMap<Character, GlyphData> glyphMap = new HashMap<>();

    public TrueTypeFont(Font font) {
        this.font = font;
        this.affineTransform = new AffineTransform();
        this.fontMetrics = new Canvas().getFontMetrics(font);
        this.context = new FontRenderContext(affineTransform, true, false);
    }

    public static int alpha(int hex) {
        return (hex >> 24) & 0xFF;
    }

    public static int red(int hex) {
        return (hex >> 16) & 0xFF;
    }

    public static int green(int hex) {
        return (hex >> 8) & 0xFF;
    }

    public static int blue(int hex) {
        return hex & 0xFF;
    }

    public int uploadTexture(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                nativeImage.setPixelRGBA(x, y, image.getRGB(x, y));
            }
        }


        DynamicTexture texture = new DynamicTexture(nativeImage);
        return texture.getId();
    }

    public int getStringHeight(String s) {
        return fontMetrics.getAscent();
    }

    public float drawCenteredShadowString(GuiGraphics guiGraphics, String str, float minX, float minY, float maxX, float maxY, int color) {
        var drawX = (minX + (maxX - minX) / 2f) - getStringWidth(str) / 2f;
        var drawY = (minY + (maxY - minY) / 2f) - getStringHeight(str) / 2f;
        drawString(guiGraphics, str, drawX + 1f, drawY + 1f, shadow);
        return drawString(guiGraphics, str, drawX, drawY, color);
    }

    public float drawShadowString(GuiGraphics guiGraphics, String str, float x, float y, int color) {
        drawString(guiGraphics, str, x + 1f, y + 1f, shadow);//影子
        return drawString(guiGraphics, str, x, y, color);
    }

    public float drawString(GuiGraphics guiGraphics, String str, float x, float y, int color) {
        PoseStack poseStack = guiGraphics.pose();


        poseStack.pushPose();
        //不这样字体就会糊，但这样做字体的xy会漂移，调用时xy要乘以Minecraft.getInstance().getWindow().getGuiScale()
        //除此之外挺完美的
        //不要用mc.options.guiScale，自动的时候是0
        var scale = 1F / (float) Minecraft.getInstance().getWindow().getGuiScale();
        poseStack.scale(scale, scale, scale);

        RenderSystem.enableBlend();

        RenderSystem.setShader(new Supplier<>() {
            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionTexShader();
            }
        });
        RenderSystem.setShaderColor(red(color) / 255F, green(color) / 255F, blue(color) / 255F, alpha(color) / 255F);

        for (int i = 0; i < str.length(); i++) {
            char targetChar = str.charAt(i);
            GlyphData glyph = getGlyphData(targetChar);
            drawGlyph(poseStack, glyph, x, y);
            x += glyph.charWidth;
        }

        RenderSystem.disableBlend();

        poseStack.popPose();

        return x;
    }

    public void drawGlyph(PoseStack matrices, GlyphData glyphData, float x, float y) {
        float xTexel = 1.0F / glyphData.width;
        float yTexel = 1.0F / glyphData.height;
        Matrix4f mat4 = matrices.last().pose();
        RenderSystem.setShaderTexture(0, glyphData.texture);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(mat4, x, y, 0.0F).uv(glyphData.u * xTexel, glyphData.v * yTexel).endVertex();
        bufferBuilder.vertex(mat4, x, y + glyphData.charHeight, 0.0F).uv(glyphData.u * xTexel, (glyphData.v + glyphData.charHeight) * yTexel).endVertex();
        bufferBuilder.vertex(mat4, x + glyphData.charWidth, y, 0.0F).uv((glyphData.u + glyphData.charWidth) * xTexel, glyphData.v * yTexel).endVertex();
        bufferBuilder.vertex(mat4, x + glyphData.charWidth, y + glyphData.charHeight, 0.0F).uv((glyphData.u + glyphData.charWidth) * xTexel, (glyphData.v + glyphData.charHeight) * yTexel).endVertex();
        Tesselator.getInstance().end();
    }

    public GlyphData getGlyphData(char character) {
        if (!glyphMap.containsKey(character)) {
            GlyphData glyphData = createGlyphData(character);
            glyphMap.put(character, glyphData);
            return glyphData;
        }
        return glyphMap.get(character);
    }

    public GlyphData createGlyphData(char character) {
        String charStr = String.valueOf(character);

        Rectangle charBounds = font.getStringBounds(charStr, context).getBounds();

        int charWidth = charBounds.width;
        int charHeight = charBounds.height;

        int imageSize = Math.max(charWidth, charHeight);

        int u = imageSize - charWidth;
        int v = imageSize - charHeight;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//不糊的关键
        graphics.drawString(charStr, u, v + getStringHeight(charStr));
        graphics.dispose();

        int texture = uploadTexture(image);
        RenderSystem.bindTexture(texture);
        return new GlyphData(character, charWidth, charHeight, imageSize, imageSize, u, v, texture);
    }

    public float getStringWidth(String s) {
        float ret = 0;
        for (int i = 0; i < s.length(); i++) {
            ret += getCharWidth(s.charAt(i));
        }
        return ret;
    }

    public float getCharWidth(char c) {
        return getGlyphData(c).charWidth;
    }

    public float getCharHeight(char c) {
        return getGlyphData(c).charHeight;
    }


    @Override
    public void close() {
        for (GlyphData glyphData : glyphMap.values()) {
            RenderSystem.deleteTexture(glyphData.texture);
        }
        glyphMap.clear();
    }


    public record GlyphData(char character, float charWidth, float charHeight, int width, int height, float u, float v,
                            int texture) {
    }
}