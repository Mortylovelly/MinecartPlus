package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import software.bernie.geckolib.core.animatable.instance.AnimatableManager;

@Mixin(SelfPropellingBoatEntity.class)
public abstract class DisableSelfPropellingBoatPaddleAnimationsMixin {

    @Inject(method = "registerControllers", at = @At("HEAD"), cancellable = true)
    private void minecartmagic$disableMissingPaddleAnimations(
            AnimatableManager.ControllerRegistrar controllers,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
