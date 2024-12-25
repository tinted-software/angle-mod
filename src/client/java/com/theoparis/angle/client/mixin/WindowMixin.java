package com.theoparis.angle.client.mixin;

import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.ThreadLocalUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

@Mixin(Window.class)
public class WindowMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    private void makeContextCurrent(long window) {
        GLFW.glfwMakeContextCurrent(window);

        GLESCapabilities gles = GLES.createCapabilities();
        // https://github.com/libgdx/libgdx/pull/7251/files#diff-7b1e3aae3e2bda381fef08f608a5b18074128f58c73c0e3c0103458d799734d5R567
        ThreadLocalUtil.setFunctionMissingAddresses(0);

        try {
            System.out.println("OpenGL ES Capabilities:");
            for (Field f : GLESCapabilities.class.getFields()) {
                if (f.getType() == boolean.class) {
                    if (f.get(gles).equals(Boolean.TRUE)) {
                        System.out.println("\t" + f.getName());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        System.out.println("GL_VENDOR: " + GLES20.glGetString(GLES20.GL_VENDOR));
        System.out.println("GL_VERSION: " + GLES20.glGetString(GLES20.GL_VERSION));
        System.out.println("GL_RENDERER: " + GLES20.glGetString(GLES20.GL_RENDERER));
    }

    // Before glfwCreateWindow is called, we need to set the context creation API to EGL and the client API to OpenGL ES
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    private long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_EGL_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
        return GLFW.glfwCreateWindow(width, height, title, monitor, share);
    }

    // Disable GL.createCapabilities() call in Window constructor
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;"))
    private GLCapabilities createCapabilities() {
        return null;
    }
}
