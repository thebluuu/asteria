package net.caffeinemc.phosphor.mixin;

import net.caffeinemc.phosphor.api.event.events.BlockBreakEvent;
import net.caffeinemc.phosphor.common.Phosphor;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.caffeinemc.phosphor.api.event.events.AttackEvent;
import net.caffeinemc.phosphor.api.event.events.ItemUseEvent;
import net.caffeinemc.phosphor.api.event.events.TickEvent;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        Phosphor.EVENTBUS.post(TickEvent.Pre.get());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onPostTick(CallbackInfo ci) {
        Phosphor.EVENTBUS.post(TickEvent.Post.get());
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onPreAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Phosphor.EVENTBUS.post(AttackEvent.Pre.get()).isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "doAttack", at = @At("TAIL"), cancellable = true)
    private void onPostAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Phosphor.EVENTBUS.post(AttackEvent.Post.get()).isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onPreItemUse(CallbackInfo ci) {
        if (Phosphor.EVENTBUS.post(ItemUseEvent.Pre.get()).isCancelled()) ci.cancel();
    }

    @Inject(method = "doItemUse", at = @At("TAIL"), cancellable = true)
    private void onPostItemUse(CallbackInfo ci) {
        if (Phosphor.EVENTBUS.post(ItemUseEvent.Post.get()).isCancelled()) ci.cancel();
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void onPreBlockBreak(boolean breaking, CallbackInfo ci) {
        if (breaking)
            if (Phosphor.EVENTBUS.post(BlockBreakEvent.Pre.get()).isCancelled()) ci.cancel();
    }

    @Inject(method = "handleBlockBreaking", at = @At("TAIL"), cancellable = true)
    private void onPostBlockBreak(boolean breaking, CallbackInfo ci) {
        if (breaking)
            if (Phosphor.EVENTBUS.post(BlockBreakEvent.Post.get()).isCancelled()) ci.cancel();
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void onStart(CallbackInfo ci) {
        new Phosphor();
        Phosphor.INSTANCE.init();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        Phosphor.configManager().saveConfig();
    }
}
