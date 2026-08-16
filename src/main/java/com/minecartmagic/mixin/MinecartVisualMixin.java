package com.minecartmagic.mixin;

import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartEntity.class)
public class MinecartVisualMixin {
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void makeEnchantedCartGlow(CallbackInfoReturnable<Boolean> cir) {
        MinecartEntity cart = (MinecartEntity) (Object) this;
        NbtCompound nbt = cart.getData();
        if (nbt != null && nbt.contains("traction_level")) {
            int level = nbt.getInt("traction_level");
            if (level > 0) {
                cir.setReturnValue(true);
            }
        }
    }
}
