package com.minecartmagic.mixin;

import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntity.class)
public class MinecartVisualMixin {

    @Inject(method = "setGlowing", at = @At("TAIL"))
    private void onSetGlowing(boolean glowing, CallbackInfo ci) {
    }
}
