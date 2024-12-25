package com.theoparis.angle.client.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Overwrite
    public void preloadPrograms(ResourceFactory factory) {
    }
}
