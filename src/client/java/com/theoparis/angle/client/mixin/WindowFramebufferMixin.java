package com.theoparis.angle.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import org.lwjgl.opengles.GLES20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static org.lwjgl.opengles.GLES20.*;
import static org.lwjgl.opengles.GLES30.GL_RGBA8;

@Mixin(WindowFramebuffer.class)
public abstract class WindowFramebufferMixin extends Framebuffer {
    public WindowFramebufferMixin(int width, int height) {
        super(true);
        this.init(width, height);
    }

    @Overwrite
    private void init(int width, int height) {
        WindowFramebuffer.Size size = this.findSuitableSize(width, height);
        this.fbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.fbo);
        GlStateManager._bindTexture(this.colorAttachment);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.colorAttachment, 0);
//        GlStateManager._bindTexture(this.depthAttachment);
//        GlStateManager._texParameter(GL_TEXTURE_2D, GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
//        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthAttachment, 0);
//        GlStateManager._bindTexture(0);
        GlStateManager._bindTexture(0);
        this.viewportWidth = size.width;
        this.viewportHeight = size.height;
        this.textureWidth = size.width;
        this.textureHeight = size.height;
        this.checkFramebufferStatus();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void initFbo(int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= i && height > 0 && height <= i) {
            this.viewportWidth = width;
            this.viewportHeight = height;
            this.textureWidth = width;
            this.textureHeight = height;
            this.fbo = GlStateManager.glGenFramebuffers();
            this.colorAttachment = TextureUtil.generateTextureId();
//            if (this.useDepthAttachment) {
//                this.depthAttachment = TextureUtil.generateTextureId();
//                GlStateManager._bindTexture(this.depthAttachment);
//                GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//                GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//                GlStateManager._texParameter(GL_TEXTURE_2D, GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
//                GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//                GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//                GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.textureWidth, this.textureHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
//            }

            this.setTexFilter(9728, true);
            GlStateManager._bindTexture(this.colorAttachment);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.textureWidth, this.textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
            GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.colorAttachment, 0);
//            if (this.useDepthAttachment) {
//                GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthAttachment, 0);
//            }

            this.checkFramebufferStatus();
            this.clear();
            this.endRead();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
        }
    }

    @Shadow
    protected abstract WindowFramebuffer.Size findSuitableSize(int width, int height);
}
