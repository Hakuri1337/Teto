package tech.hakuri.teto.mc1201;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

/**
 * 面板背景模糊（1.20.1 only）。
 * <p>
 * 照 GuardLite {@code BlurUtils} 的思路：把主渲染目标当前的颜色纹理过一遍可分离高斯模糊
 * （横向 + 纵向，两个半分辨率 ping-pong FBO），再把模糊结果以「纹理化圆角矩形」贴回面板区域。
 * 与 GuardLite 的差别（都是为了少干活 + 不侵入原版管线）：
 * <ul>
 *   <li><b>不用 stencil</b>。GuardLite 用 stencil 裁出圆角形状，需要往主 FBO 挂模板缓冲、
 *       还得反射改 {@code depthBufferId}，对原版渲染管线侵入大，而且会破坏深度缓冲。
 *       这里改成把模糊纹理直接画成「带 UV 的圆角几何」——圆角由几何细分决定，效果一样，零侵入。</li>
 *   <li><b>2 段可分离高斯代替 6 段 Kawase 链</b>。面板模糊这个用途下视觉差异不可分辨，少一半 FBO 和代码。</li>
 *   <li><b>GLSL 内嵌成 Java 字符串</b>，不依赖外部 shader 资源文件。GuardLite 的 shader 源码
 *       根本不在仓库里、靠运行时文件系统加载，崩了连报错都难看；内嵌就稳定。</li>
 * </ul>
 * <p>
 * 所有 GL 资源单例、懒加载、只在渲染线程碰。窗口尺寸变了 FBO 重建。
 */
final class BlurRenderer1201 {

    private static final BlurRenderer1201 INSTANCE = new BlurRenderer1201();

    static BlurRenderer1201 get() {
        return INSTANCE;
    }

    private BlurRenderer1201() {
    }

    // ===== GL 资源（懒加载，单例常驻）=====
    private boolean initialized;
    private int blurProgram;
    private int uTextureLoc;
    private int uDirectionLoc;
    private int quadVao;
    private int quadVbo;

    // 两个半分辨率 ping-pong FBO
    private int fboA, texA;
    private int fboB, texB;
    private int fboW, fboH;
    private boolean fbosDirty = true;

    // ===== GLSL（内嵌，#version 330，OpenGL 3.3+，1.20.1 环境稳支持）=====

    /** 全屏三角形带的顶点着色器：忽略 MC 矩阵（FBO pass 不在 GUI pose 下），位置 -1..1，UV 0..1。 */
    private static final String VERT_SRC =
            "#version 330 core\n" +
            "layout(location = 0) in vec2 aPosition;\n" +
            "out vec2 vUv;\n" +
            "void main() {\n" +
            "    vUv = aPosition * 0.5 + 0.5;\n" +
            "    gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
            "}\n";

    /** 可分离高斯：uDirection 决定方向（横向 = (r/w,0)，纵向 = (0,r/h)）。9 抽头，权重和=1。 */
    private static final String FRAG_SRC =
            "#version 330 core\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform vec2 uDirection;\n" +
            "in vec2 vUv;\n" +
            "out vec4 fragColor;\n" +
            "void main() {\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    sum += texture(uTexture, vUv + uDirection * -4.0) * 0.0162162162;\n" +
            "    sum += texture(uTexture, vUv + uDirection * -3.0) * 0.0540540541;\n" +
            "    sum += texture(uTexture, vUv + uDirection * -2.0) * 0.1216216216;\n" +
            "    sum += texture(uTexture, vUv + uDirection * -1.0) * 0.1945945946;\n" +
            "    sum += texture(uTexture, vUv) * 0.2270270270;\n" +
            "    sum += texture(uTexture, vUv + uDirection * 1.0) * 0.1945945946;\n" +
            "    sum += texture(uTexture, vUv + uDirection * 2.0) * 0.1216216216;\n" +
            "    sum += texture(uTexture, vUv + uDirection * 3.0) * 0.0540540541;\n" +
            "    sum += texture(uTexture, vUv + uDirection * 4.0) * 0.0162162162;\n" +
            "    fragColor = sum;\n" +
            "}\n";

    /** 高斯模糊半径（以主纹理 texel 为单位）。4 已经够面板柔化，太大会糊到看不清字。 */
    private static final float BLUR_RADIUS_TEXELS = 4.0F;

    // ===== 初始化 =====

    private void initGl() {
        int vert = compileShader(GL20.GL_VERTEX_SHADER, VERT_SRC);
        int frag = compileShader(GL20.GL_FRAGMENT_SHADER, FRAG_SRC);
        blurProgram = GL20.glCreateProgram();
        GL20.glAttachShader(blurProgram, vert);
        GL20.glAttachShader(blurProgram, frag);
        GL20.glBindAttribLocation(blurProgram, 0, "aPosition");
        GL20.glLinkProgram(blurProgram);
        if (GL20.glGetProgrami(blurProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("blur program link failed: " + GL20.glGetProgramInfoLog(blurProgram));
        }
        GL20.glDeleteShader(vert);
        GL20.glDeleteShader(frag);
        uTextureLoc = GL20.glGetUniformLocation(blurProgram, "uTexture");
        uDirectionLoc = GL20.glGetUniformLocation(blurProgram, "uDirection");

        // 全屏四（两个三角形），覆盖 -1..1，给 FBO pass 用
        float[] quad = {-1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1};
        quadVao = GL30.glGenVertexArrays();
        quadVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(quadVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        FloatBuffer fb = BufferUtils.createFloatBuffer(quad.length);
        fb.put(quad).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
        GL30.glBindVertexArray(0);

        initialized = true;
        System.out.println("[teto] 面板模糊管线就绪（9 抽头可分离高斯，2× 半分辨率 FBO）");
    }

    private static int compileShader(int type, String src) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, src);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("blur shader compile failed: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    // ===== FBO =====

    private void ensureFbos(RenderTarget main) {
        int w = Math.max(1, main.width / 2);
        int h = Math.max(1, main.height / 2);
        if (!fbosDirty && fboW == w && fboH == h) return;
        deleteFbos();
        fboA = GL30.glGenFramebuffers();
        texA = GL11.glGenTextures();
        initFbo(fboA, texA, w, h);
        fboB = GL30.glGenFramebuffers();
        texB = GL11.glGenTextures();
        initFbo(fboB, texB, w, h);
        fboW = w;
        fboH = h;
        fbosDirty = false;
    }

    private static void initFbo(int fbo, int tex, int w, int h) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
    }

    private void deleteFbos() {
        if (fboA != 0) GL30.glDeleteFramebuffers(fboA);
        if (fboB != 0) GL30.glDeleteFramebuffers(fboB);
        if (texA != 0) GL11.glDeleteTextures(texA);
        if (texB != 0) GL11.glDeleteTextures(texB);
        fboA = fboB = texA = texB = 0;
        fbosDirty = true;
    }

    // ===== 主入口 =====

    /**
     * 把主渲染目标当前的颜色纹理模糊后贴回 (x,y,w,h) 圆角面板区域。
     * <b>必须在面板其它绘制之前调用</b>——此时主纹理里还是面板「后面」的场景。
     */
    void renderBlur(GuiGraphics gui, float x, float y, float w, float h, float radius) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) return;
        RenderTarget main = mc.getMainRenderTarget();
        if (main == null || w <= 1F || h <= 1F) return;
        try {
            if (!initialized) initGl();
            ensureFbos(main);

            int mainW = main.width;
            int mainH = main.height;

            // Pass 1：主纹理 → FBO_A（半分辨率，横向高斯）
            // uDirection 用主纹理 texel size——两张 FBO 的 UV [0,1] 都对应全屏，
            // 所以两 pass 用同一份主 texel 半径，屏幕空间模糊量才对称。
            blurPass(fboA, fboW, fboH, main.getColorTextureId(), BLUR_RADIUS_TEXELS / mainW, 0F);
            // Pass 2：FBO_A → FBO_B（纵向高斯）
            blurPass(fboB, fboW, fboH, texA, 0F, BLUR_RADIUS_TEXELS / mainH);

            // 合成：把 FBO_B 贴成圆角矩形回到主目标
            main.bindWrite(false);
            GL11.glViewport(0, 0, mainW, mainH);
            GL20.glUseProgram(0); // 把程序归零，让 MC 的 getPositionTexShader 接管合成
            GL30.glBindVertexArray(0); // 别让我们的 VAO 污染 MC 后续顶点状态
            drawTexturedRoundedRect(gui, texB, x, y, w, h, radius, mainW, mainH, (float) mc.getWindow().getGuiScale());
        } catch (Throwable t) {
            System.err.println("[teto] 面板模糊失败，本次跳过：" + t);
            t.printStackTrace(System.err);
            try {
                mc.getMainRenderTarget().bindWrite(false);
                GL20.glUseProgram(0);
            } catch (Throwable ignored) {
            }
        }
    }

    /** 一个 FBO pass：绑 targetFbo、设视口、绑 srcTex、画全屏四、跑 blur 着色器。 */
    private void blurPass(int targetFbo, int targetW, int targetH, int srcTex, float dirX, float dirY) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, targetFbo);
        GL11.glViewport(0, 0, targetW, targetH);
        // FBO pass 不需要混合：9 抽头权重和=1，输出不透明，直接覆盖
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glUseProgram(blurProgram);
        GL20.glUniform1i(uTextureLoc, 0);
        GL20.glUniform2f(uDirectionLoc, dirX, dirY);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, srcTex);
        GL30.glBindVertexArray(quadVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // ===== 合成：带 UV 的圆角矩形（复刻 drawRoundedRect 的 5 矩形 + 4 扇形，但走 POSITION_TEX）=====

    private void drawTexturedRoundedRect(GuiGraphics gui, int tex, float x, float y, float w, float h,
                                         float radius, int mainW, int mainH, float scale) {
        if (radius < 0F) radius = 0F;
        if (radius > w / 2F) radius = w / 2F;
        if (radius > h / 2F) radius = h / 2F;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        Matrix4f matrix = gui.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // 5 个矩形（QUADS）
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        texQuad(buffer, matrix, x + radius, y + radius, w - radius * 2F, h - radius * 2F, x, y, mainW, mainH, scale);
        texQuad(buffer, matrix, x + radius, y, w - radius * 2F, radius, x, y, mainW, mainH, scale);
        texQuad(buffer, matrix, x + radius, y + h - radius, w - radius * 2F, radius, x, y, mainW, mainH, scale);
        texQuad(buffer, matrix, x, y + radius, radius, h - radius * 2F, x, y, mainW, mainH, scale);
        texQuad(buffer, matrix, x + w - radius, y + radius, radius, h - radius * 2F, x, y, mainW, mainH, scale);
        Tesselator.getInstance().end();

        // 4 个角（TRIANGLE_FAN），顶点数随半径走，与 drawRoundedRect 一致
        int vertices = (int) Math.min(Math.max(radius * 2.5F, 12.0F), 90.0F);
        texFan(buffer, matrix, x + radius, y + radius, radius, vertices, 180, x, y, mainW, mainH, scale);
        texFan(buffer, matrix, x + w - radius, y + radius, radius, vertices, 90, x, y, mainW, mainH, scale);
        texFan(buffer, matrix, x + radius, y + h - radius, radius, vertices, 270, x, y, mainW, mainH, scale);
        texFan(buffer, matrix, x + w - radius, y + h - radius, radius, vertices, 0, x, y, mainW, mainH, scale);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    /**
     * 把 GUI 坐标 (px,py) 映射到主纹理 UV：
     * u = px*scale/mainW（屏幕横向归一），
     * v = 1 - py*scale/mainH（主纹理原点在左下，GUI 原点在左上，所以 V 要翻转）。
     */
    private static void texVert(BufferBuilder b, Matrix4f m, float px, float py,
                                float originX, float originY, int mainW, int mainH, float scale) {
        float u = px * scale / mainW;
        float v = 1F - py * scale / mainH;
        b.vertex(m, px, py, 0F).uv(u, v).endVertex();
    }

    private static void texQuad(BufferBuilder b, Matrix4f m, float qx, float qy, float qw, float qh,
                               float originX, float originY, int mainW, int mainH, float scale) {
        texVert(b, m, qx, qy, originX, originY, mainW, mainH, scale);
        texVert(b, m, qx + qw, qy, originX, originY, mainW, mainH, scale);
        texVert(b, m, qx + qw, qy + qh, originX, originY, mainW, mainH, scale);
        texVert(b, m, qx, qy + qh, originX, originY, mainW, mainH, scale);
    }

    private static void texFan(BufferBuilder b, Matrix4f m, float cx, float cy, float rad, int vertices, int quadrant,
                              float originX, float originY, int mainW, int mainH, float scale) {
        b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        texVert(b, m, cx, cy, originX, originY, mainW, mainH, scale);
        for (int i = 0; i <= vertices; i++) {
            double angle = (Math.PI * 2.0) * (i + quadrant) / (vertices * 4.0);
            float px = (float) (cx + Math.sin(angle) * rad);
            float py = (float) (cy + Math.cos(angle) * rad);
            texVert(b, m, px, py, originX, originY, mainW, mainH, scale);
        }
        Tesselator.getInstance().end();
    }
}
