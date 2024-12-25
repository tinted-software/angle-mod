package com.theoparis.angle.client.mixin;

import com.mojang.blaze3d.platform.GLX;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.util.Untracker;
import org.lwjgl.opengles.GLDebugMessageKHRCallback;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.opengles.KHRDebug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GlDebug.class)
public class GlDebugMixin {
    @Shadow
    private static boolean debugMessageEnabled;
    @Final
    @Shadow
    private static List<Integer> KHR_VERBOSITY_LEVELS;
    @Final
    @Shadow
    private static List<Integer> ARB_VERBOSITY_LEVELS;

    @Overwrite
    public static void enableDebug(int verbosity, boolean sync) {
        if (verbosity > 0) {
            KHRDebug.glDebugMessageCallbackKHR(GLX.make(GLDebugMessageKHRCallback.create(GlDebugMixin::info), Untracker::untrack), 0L);
            debugMessageEnabled = true;
            GLES20.glEnable(37600);
            if (sync) {
                GLES20.glEnable(33346);
            }

            for (int i = 0; i < KHR_VERBOSITY_LEVELS.size(); ++i) {
                boolean bl = i < verbosity;
                KHRDebug.glDebugMessageControlKHR(4352, 4352, (Integer) KHR_VERBOSITY_LEVELS.get(i), (int[]) null, bl);
            }
        }
    }

    @Shadow
    private static void info(int i, int i1, int i2, int i3, int i4, long l, long l1) {

    }
}
