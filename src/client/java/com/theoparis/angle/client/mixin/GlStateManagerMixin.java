package com.theoparis.angle.client.mixin;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.Plot;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.MacWindowUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengles.GLES30;
import org.lwjgl.opengles.GLES31;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengles.OESMapbuffer.glMapBufferOES;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Final
    @Shadow
    private static boolean ON_LINUX;
    @Final
    @Shadow
    private static Plot PLOT_TEXTURES;
    @Shadow
    private static int numTextures;
    @Final
    @Shadow
    private static Plot PLOT_BUFFERS;
    @Shadow
    private static int numBuffers;
    @Final
    @Shadow
    private static GlStateManager.BlendFuncState BLEND;
    @Final
    @Shadow
    private static GlStateManager.DepthTestState DEPTH;
    @Final
    @Shadow
    private static GlStateManager.CullFaceState CULL;
    @Final
    @Shadow
    private static GlStateManager.PolygonOffsetState POLY_OFFSET;
    @Final
    @Shadow
    private static GlStateManager.LogicOpState COLOR_LOGIC;
    @Final
    @Shadow
    private static GlStateManager.StencilState STENCIL;
    @Final
    @Shadow
    private static GlStateManager.ScissorTestState SCISSOR;
    @Final
    @Shadow
    private static GlStateManager.FramebufferState READ_FRAMEBUFFER;
    @Final
    @Shadow
    private static GlStateManager.FramebufferState DRAW_FRAMEBUFFER;
    @Shadow
    private static int activeTexture;
    @Final
    @Shadow
    private static GlStateManager.Texture2DState[] TEXTURES;
    @Final
    @Shadow
    private static GlStateManager.ColorMask COLOR_MASK;

    @Overwrite(remap = false)
    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        SCISSOR.capState.enable();
    }

    @Overwrite(remap = false)
    public static void _scissorBox(int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glScissor(x, y, width, height);
    }

    @Overwrite(remap = false)
    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        DEPTH.capState.disable();
    }

    @Overwrite(remap = false)
    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        DEPTH.capState.enable();
    }

    @Overwrite(remap = false)
    public static void _depthFunc(int func) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (func != DEPTH.func) {
            DEPTH.func = func;
            GLES30.glDepthFunc(func);
        }

    }

    @Overwrite(remap = false)
    public static void _depthMask(boolean mask) {
        RenderSystem.assertOnRenderThread();
        if (mask != DEPTH.mask) {
            DEPTH.mask = mask;
            GLES30.glDepthMask(mask);
        }
    }

    @Overwrite(remap = false)
    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        BLEND.capState.disable();
    }

    @Overwrite(remap = false)
    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        BLEND.capState.enable();
    }

    @Overwrite(remap = false)
    public static void _blendFunc(int srcFactor, int dstFactor) {
        RenderSystem.assertOnRenderThread();
        if (srcFactor != BLEND.srcFactorRGB || dstFactor != BLEND.dstFactorRGB) {
            BLEND.srcFactorRGB = srcFactor;
            BLEND.dstFactorRGB = dstFactor;
            GLES30.glBlendFunc(srcFactor, dstFactor);
        }

    }

    @Overwrite(remap = false)
    public static void _blendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
        RenderSystem.assertOnRenderThread();
        if (srcFactorRGB != BLEND.srcFactorRGB || dstFactorRGB != BLEND.dstFactorRGB || srcFactorAlpha != BLEND.srcFactorAlpha || dstFactorAlpha != BLEND.dstFactorAlpha) {
            BLEND.srcFactorRGB = srcFactorRGB;
            BLEND.dstFactorRGB = dstFactorRGB;
            BLEND.srcFactorAlpha = srcFactorAlpha;
            BLEND.dstFactorAlpha = dstFactorAlpha;
            glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
        }

    }

    @Overwrite(remap = false)
    public static void _blendEquation(int mode) {
        RenderSystem.assertOnRenderThread();
        GLES30.glBlendEquation(mode);
    }

    @Overwrite(remap = false)
    public static int glGetProgrami(int program, int pname) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetProgrami(program, pname);
    }

    @Overwrite(remap = false)
    public static void glAttachShader(int program, int shader) {
        RenderSystem.assertOnRenderThread();
        GLES30.glAttachShader(program, shader);
    }

    @Overwrite(remap = false)
    public static void glDeleteShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GLES30.glDeleteShader(shader);
    }

    @Overwrite(remap = false)
    public static int glCreateShader(int type) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glCreateShader(type);
    }

    @Overwrite(remap = false)
    public static void glShaderSource(int shader, String string) {
        RenderSystem.assertOnRenderThread();
        byte[] bs = string.getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length + 1);
        byteBuffer.put(bs);
        byteBuffer.put((byte) 0);
        byteBuffer.flip();

        try {
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
                PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                pointerBuffer.put(byteBuffer);
                GLES30.nglShaderSource(shader, 1, pointerBuffer.address0(), 0L);
            } catch (Throwable var12) {
                if (memoryStack != null) {
                    try {
                        memoryStack.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                }

                throw var12;
            }

            if (memoryStack != null) {
                memoryStack.close();
            }
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }

    }

    @Overwrite(remap = false)
    public static void glCompileShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GLES30.glCompileShader(shader);
    }

    @Overwrite(remap = false)
    public static int glGetShaderi(int shader, int pname) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetShaderi(shader, pname);
    }

    @Overwrite(remap = false)
    public static void _glUseProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUseProgram(program);
    }

    @Overwrite(remap = false)
    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GLES30.glCreateProgram();
    }

    @Overwrite(remap = false)
    public static void glDeleteProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GLES30.glDeleteProgram(program);
    }

    @Overwrite(remap = false)
    public static void glLinkProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GLES30.glLinkProgram(program);
    }

    @Overwrite(remap = false)
    public static int _glGetUniformLocation(int program, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetUniformLocation(program, name);
    }

    @Overwrite(remap = false)
    public static void _glUniform1(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform1iv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform1i(int location, int value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform1i(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform1(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform1fv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform2(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform2iv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform2(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform2fv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform3(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform3iv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform3(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform3fv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform4(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform4iv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniform4(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniform4fv(location, value);
    }

    @Overwrite(remap = false)
    public static void _glUniformMatrix2(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniformMatrix2fv(location, transpose, value);
    }

    @Overwrite(remap = false)
    public static void _glUniformMatrix3(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniformMatrix3fv(location, transpose, value);
    }

    @Overwrite(remap = false)
    public static void _glUniformMatrix4(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GLES30.glUniformMatrix4fv(location, transpose, value);
    }

    @Overwrite(remap = false)
    public static int _glGetAttribLocation(int program, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetAttribLocation(program, name);
    }

    @Overwrite(remap = false)
    public static void _glBindAttribLocation(int program, int index, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        GLES30.glBindAttribLocation(program, index, name);
    }

    @Overwrite(remap = false)
    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        ++numBuffers;
        PLOT_BUFFERS.setValue((double) numBuffers);
        return GLES30.glGenBuffers();
    }

    @Overwrite(remap = false)
    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glGenVertexArrays();
    }

    @Overwrite(remap = false)
    public static void _glBindBuffer(int target, int buffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBindBuffer(target, buffer);
    }

    @Overwrite(remap = false)
    public static void _glBindVertexArray(int array) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBindVertexArray(array);
    }

    @Overwrite(remap = false)
    public static void _glBufferData(int target, ByteBuffer data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBufferData(target, data, usage);
    }

    @Overwrite(remap = false)
    public static void _glBufferSubData(int i, int j, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBufferSubData(i, (long) j, byteBuffer);
    }

    @Overwrite(remap = false)
    public static void _glBufferData(int target, long size, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBufferData(target, size, usage);
    }

    @Nullable
    @Overwrite(remap = false)
    public static ByteBuffer mapBuffer(int target, int access) {
        RenderSystem.assertOnRenderThreadOrInit();
        return glMapBufferOES(target, access);
    }

    @Nullable
    @Overwrite(remap = false)
    public static ByteBuffer _glMapBufferRange(int target, int offset, int length, int access) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glMapBufferRange(target, (long) offset, (long) length, access);
    }

    @Overwrite(remap = false)
    public static void _glUnmapBuffer(int target) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glUnmapBuffer(target);
    }

    @Overwrite(remap = false)
    public static void _glDeleteBuffers(int buffer) {
        RenderSystem.assertOnRenderThread();
        if (ON_LINUX) {
            GLES30.glBindBuffer(34962, buffer);
            GLES30.glBufferData(34962, 0L, 35048);
            GLES30.glBindBuffer(34962, 0);
        }

        --numBuffers;
        PLOT_BUFFERS.setValue((double) numBuffers);
        GLES30.glDeleteBuffers(buffer);
    }

    @Overwrite(remap = false)
    public static void _glCopyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
    }

    @Overwrite(remap = false)
    public static void _glDeleteVertexArrays(int array) {
        RenderSystem.assertOnRenderThread();
        GLES30.glDeleteVertexArrays(array);
    }

    @Overwrite(remap = false)
    public static void _glBindFramebuffer(int target, int framebuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        boolean var10000;
        switch (target) {
            case 36008 -> var10000 = READ_FRAMEBUFFER.setBoundFramebuffer(framebuffer);
            case 36009 -> var10000 = DRAW_FRAMEBUFFER.setBoundFramebuffer(framebuffer);
            case 36160 ->
                    var10000 = READ_FRAMEBUFFER.setBoundFramebuffer(framebuffer) | DRAW_FRAMEBUFFER.setBoundFramebuffer(framebuffer);
            default -> var10000 = true;
        }

        boolean bl = var10000;
        if (bl) {
            GLES30.glBindFramebuffer(target, framebuffer);
        }

    }

    @Overwrite(remap = false)
    public static void _glBlitFrameBuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Overwrite(remap = false)
    public static void _glBindRenderbuffer(int target, int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glBindRenderbuffer(target, renderbuffer);
    }

    @Overwrite(remap = false)
    public static void _glDeleteRenderbuffers(int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glDeleteRenderbuffers(renderbuffer);
    }

    @Overwrite(remap = false)
    public static void _glDeleteFramebuffers(int framebuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glDeleteFramebuffers(framebuffer);
    }

    @Overwrite(remap = false)
    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glGenFramebuffers();
    }

    @Overwrite(remap = false)
    public static int glGenRenderbuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glGenRenderbuffers();
    }

    @Overwrite(remap = false)
    public static void _glRenderbufferStorage(int target, int internalFormat, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glRenderbufferStorage(target, internalFormat, width, height);
    }

    @Overwrite(remap = false)
    public static void _glFramebufferRenderbuffer(int target, int attachment, int renderbufferTarget, int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
    }

    @Overwrite(remap = false)
    public static int glCheckFramebufferStatus(int target) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glCheckFramebufferStatus(target);
    }

    @Overwrite(remap = false)
    public static void _glFramebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }

    @Overwrite(remap = false)
    public static int getBoundFramebuffer() {
        RenderSystem.assertOnRenderThread();
        return _getInteger(36006);
    }

    @Overwrite(remap = false)
    public static void glActiveTexture(int texture) {
        RenderSystem.assertOnRenderThread();
        GLES30.glActiveTexture(texture);
    }

    @Overwrite(remap = false)
    public static void glBlendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
        RenderSystem.assertOnRenderThread();
        GLES30.glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
    }

    @Overwrite(remap = false)
    public static String glGetShaderInfoLog(int shader, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetShaderInfoLog(shader, maxLength);
    }

    @Overwrite(remap = false)
    public static String glGetProgramInfoLog(int program, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetProgramInfoLog(program, maxLength);
    }

    @Overwrite(remap = false)
    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderLights(matrix4f.transformDirection(vector3f, new Vector3f()), matrix4f.transformDirection(vector3f2, new Vector3f()));
    }

    @Overwrite(remap = false)
    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = (new Matrix4f()).rotationY((-(float) Math.PI / 8F)).rotateX(2.3561945F);
        setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    @Overwrite(remap = false)
    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = (new Matrix4f()).scaling(1.0F, -1.0F, 1.0F).rotateYXZ(1.0821041F, 3.2375858F, 0.0F).rotateYXZ((-(float) Math.PI / 8F), 2.3561945F, 0.0F);
        setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    @Overwrite(remap = false)
    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        CULL.capState.enable();
    }

    @Overwrite(remap = false)
    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        CULL.capState.disable();
    }

    @Overwrite(remap = false)
    public static void _polygonMode(int face, int mode) {
        RenderSystem.assertOnRenderThread();
        System.err.println("glPolygonMode is not supported on GLES");
    }

    @Overwrite(remap = false)
    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        POLY_OFFSET.capFill.enable();
    }

    @Overwrite(remap = false)
    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        POLY_OFFSET.capFill.disable();
    }

    @Overwrite(remap = false)
    public static void _polygonOffset(float factor, float units) {
        RenderSystem.assertOnRenderThread();
        if (factor != POLY_OFFSET.factor || units != POLY_OFFSET.units) {
            POLY_OFFSET.factor = factor;
            POLY_OFFSET.units = units;
            GLES30.glPolygonOffset(factor, units);
        }

    }

    @Overwrite(remap = false)
    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        COLOR_LOGIC.capState.enable();
    }

    @Overwrite(remap = false)
    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        COLOR_LOGIC.capState.disable();
    }

    @Overwrite(remap = false)
    public static void _logicOp(int op) {
        RenderSystem.assertOnRenderThread();
        System.err.println("glLogicOp is not supported on GLES");
    }

    @Overwrite(remap = false)
    public static void _activeTexture(int texture) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != texture - '蓀') {
            activeTexture = texture - '蓀';
            glActiveTexture(texture);
        }

    }

    @Overwrite(remap = false)
    public static void _texParameter(int target, int pname, float param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glTexParameterf(target, pname, param);
    }

    @Overwrite(remap = false)
    public static void _texParameter(int target, int pname, int param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glTexParameteri(target, pname, param);
    }

    @Overwrite(remap = false)
    public static int _getTexLevelParameter(int target, int level, int pname) {
        return GLES31.glGetTexLevelParameteri(target, level, pname);
    }

    @Overwrite(remap = false)
    public static int _genTexture() {
        RenderSystem.assertOnRenderThreadOrInit();
        ++numTextures;
        PLOT_TEXTURES.setValue((double) numTextures);
        return GLES30.glGenTextures();
    }

    @Overwrite(remap = false)
    public static void _genTextures(int[] textures) {
        RenderSystem.assertOnRenderThreadOrInit();
        numTextures += textures.length;
        PLOT_TEXTURES.setValue((double) numTextures);
        GLES30.glGenTextures(textures);
    }


    @Overwrite(remap = false)
    public static void _deleteTexture(int texture) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glDeleteTextures(texture);

        for (com.mojang.blaze3d.platform.GlStateManager.Texture2DState texture2DState : TEXTURES) {
            if (texture2DState.boundTexture == texture) {
                texture2DState.boundTexture = -1;
            }
        }

        --numTextures;
        PLOT_TEXTURES.setValue((double) numTextures);
    }

    @Overwrite(remap = false)
    public static void _deleteTextures(int[] textures) {
        RenderSystem.assertOnRenderThreadOrInit();

        for (com.mojang.blaze3d.platform.GlStateManager.Texture2DState texture2DState : TEXTURES) {
            for (int i : textures) {
                if (texture2DState.boundTexture == i) {
                    texture2DState.boundTexture = -1;
                }
            }
        }

        GLES30.glDeleteTextures(textures);
        numTextures -= textures.length;
        PLOT_TEXTURES.setValue((double) numTextures);
    }

    @Overwrite(remap = false)
    public static void _bindTexture(int texture) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (texture != TEXTURES[activeTexture].boundTexture) {
            TEXTURES[activeTexture].boundTexture = texture;
            GLES30.glBindTexture(3553, texture);
        }

    }

    @Overwrite(remap = false)
    public static int _getActiveTexture() {
        return activeTexture + '蓀';
    }

    @Overwrite(remap = false)
    public static void _texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, @Nullable IntBuffer pixels) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    @Overwrite(remap = false)
    public static void _texSubImage2D(int target, int level, int offsetX, int offsetY, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glTexSubImage2D(target, level, offsetX, offsetY, width, height, format, type, pixels);
    }

    @Overwrite(remap = false)
    public static void upload(int level, int offsetX, int offsetY, int width, int height, NativeImage.Format format, IntBuffer pixels, Consumer<IntBuffer> closer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> _upload(level, offsetX, offsetY, width, height, format, pixels, closer));
        } else {
            _upload(level, offsetX, offsetY, width, height, format, pixels, closer);
        }

    }

    @Overwrite(remap = false)
    public static void _upload(int level, int offsetX, int offsetY, int width, int height, NativeImage.Format format, IntBuffer pixels, Consumer<IntBuffer> closer) {
        try {
            RenderSystem.assertOnRenderThreadOrInit();
            _pixelStore(3314, width);
            _pixelStore(3316, 0);
            _pixelStore(3315, 0);
            format.setUnpackAlignment();
            GLES30.glTexSubImage2D(3553, level, offsetX, offsetY, width, height, format.toGl(), 5121, pixels);
        } finally {
            closer.accept(pixels);
        }

    }

    @Overwrite(remap = false)
    public static void _getTexImage(int target, int level, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        System.err.println("glGetTexImage is not supported on GLES");
    }

    @Overwrite(remap = false)
    public static void _viewport(int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.Viewport.INSTANCE.x = x;
        GlStateManager.Viewport.INSTANCE.y = y;
        GlStateManager.Viewport.INSTANCE.width = width;
        GlStateManager.Viewport.INSTANCE.height = height;
        GLES30.glViewport(x, y, width, height);
    }

    @Overwrite(remap = false)
    public static void _colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        RenderSystem.assertOnRenderThread();
        if (red != COLOR_MASK.red || green != COLOR_MASK.green || blue != COLOR_MASK.blue || alpha != COLOR_MASK.alpha) {
            COLOR_MASK.red = red;
            COLOR_MASK.green = green;
            COLOR_MASK.blue = blue;
            COLOR_MASK.alpha = alpha;
            GLES30.glColorMask(red, green, blue, alpha);
        }

    }

    @Overwrite(remap = false)
    public static void _stencilFunc(int func, int ref, int mask) {
        RenderSystem.assertOnRenderThread();
        if (func != STENCIL.subState.func || func != STENCIL.subState.ref || func != STENCIL.subState.mask) {
            STENCIL.subState.func = func;
            STENCIL.subState.ref = ref;
            STENCIL.subState.mask = mask;
            GLES30.glStencilFunc(func, ref, mask);
        }

    }

    @Overwrite(remap = false)
    public static void _stencilMask(int mask) {
        RenderSystem.assertOnRenderThread();
        if (mask != STENCIL.mask) {
            STENCIL.mask = mask;
            GLES30.glStencilMask(mask);
        }

    }

    @Overwrite(remap = false)
    public static void _stencilOp(int sfail, int dpfail, int dppass) {
        RenderSystem.assertOnRenderThread();
        if (sfail != STENCIL.sfail || dpfail != STENCIL.dpfail || dppass != STENCIL.dppass) {
            STENCIL.sfail = sfail;
            STENCIL.dpfail = dpfail;
            STENCIL.dppass = dppass;
            GLES30.glStencilOp(sfail, dpfail, dppass);
        }

    }

    @Overwrite(remap = false)
    public static void _clearDepth(double depth) {
        RenderSystem.assertOnRenderThreadOrInit();
        System.err.println("glClearDepth is not supported on GLES");
    }

    @Overwrite(remap = false)
    public static void _clearColor(float red, float green, float blue, float alpha) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glClearColor(red, green, blue, alpha);
    }

    @Overwrite(remap = false)
    public static void _clearStencil(int stencil) {
        RenderSystem.assertOnRenderThread();
        GLES30.glClearStencil(stencil);
    }

    @Overwrite(remap = false)
    public static void _clear(int mask) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glClear(mask);
        if (MacWindowUtil.field_52734) {
            _getError();
        }

    }

    @Overwrite(remap = false)
    public static void _glDrawPixels(int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        System.err.println("glDrawPixels is not supported on GLES");
    }

    @Overwrite(remap = false)
    public static void _vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
        RenderSystem.assertOnRenderThread();
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    @Overwrite(remap = false)
    public static void _vertexAttribIPointer(int index, int size, int type, int stride, long pointer) {
        RenderSystem.assertOnRenderThread();
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    @Overwrite(remap = false)
    public static void _enableVertexAttribArray(int index) {
        RenderSystem.assertOnRenderThread();
        GLES30.glEnableVertexAttribArray(index);
    }

    @Overwrite(remap = false)
    public static void _disableVertexAttribArray(int index) {
        RenderSystem.assertOnRenderThread();
        GLES30.glDisableVertexAttribArray(index);
    }

    @Overwrite(remap = false)
    public static void _drawElements(int mode, int count, int type, long indices) {
        RenderSystem.assertOnRenderThread();
        GLES30.glDrawElements(mode, count, type, indices);
    }

    @Overwrite(remap = false)
    public static void _pixelStore(int pname, int param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glPixelStorei(pname, param);
    }

    @Overwrite(remap = false)
    public static void _readPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        RenderSystem.assertOnRenderThread();
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Overwrite(remap = false)
    public static void _readPixels(int x, int y, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Overwrite(remap = false)
    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetError();
    }

    @Overwrite(remap = false)
    public static String _getString(int name) {
        RenderSystem.assertOnRenderThread();
        return GLES30.glGetString(name);
    }

    @Overwrite(remap = false)
    public static int _getInteger(int pname) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glGetInteger(pname);
    }

    @Overwrite(remap = false)
    public static long _glFenceSync(int condition, int flags) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glFenceSync(condition, flags);
    }

    @Overwrite(remap = false)
    public static int _glClientWaitSync(long sync, int flags, long timeoutNanos) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GLES30.glClientWaitSync(sync, flags, timeoutNanos);
    }

    @Overwrite(remap = false)
    public static void _glDeleteSync(long sync) {
        RenderSystem.assertOnRenderThreadOrInit();
        GLES30.glDeleteSync(sync);
    }
}
