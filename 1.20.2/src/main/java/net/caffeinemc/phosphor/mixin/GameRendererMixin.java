package net.caffeinemc.phosphor.mixin;

import net.caffeinemc.phosphor.api.event.events.WorldRenderEvent;
import net.caffeinemc.phosphor.common.Phosphor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE))
    private void onPostWorldRender(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        Phosphor.EVENTBUS.post(WorldRenderEvent.get(matrices, tickDelta));
    }
}
