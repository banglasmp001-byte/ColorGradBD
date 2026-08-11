package com.ahmednotxgamer.colorgradebd.mixin;

import com.ahmednotxgamer.colorgradebd.client.render.ColorGradingRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "onResized(II)V", at = @At("TAIL"), require = 0)
    private void colorgradebd_onResize(int width, int height, CallbackInfo ci) {
        ColorGradingRenderer.getInstance().onWindowResized(width, height);
    }
}
