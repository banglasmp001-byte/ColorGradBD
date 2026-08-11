package com.ahmednotxgamer.colorgradebd.mixin;

import com.ahmednotxgamer.colorgradebd.client.render.ColorGradingRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into GameRenderer to notify our renderer when the window is resized,
 * allowing the post-effect processor to resize its framebuffers correctly.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "resize(II)V", at = @At("TAIL"))
    private void colorgradebd_onResize(int width, int height, CallbackInfo ci) {
        ColorGradingRenderer.getInstance().onWindowResized(width, height);
    }
}
