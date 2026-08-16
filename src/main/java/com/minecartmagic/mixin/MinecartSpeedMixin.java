package com.minecartmagic.mixin;

import com.minecartmagic.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public class MinecartSpeedMixin {

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$modifyMaxSpeed(CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;

        var world = minecart.getWorld();

        int level = EnchantmentHelper.getLevel(
                world.getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(ModEnchantments.TRACTION_KEY),
                minecart.getPickBlockStack()
        );

        if (level > 0) {
            double multiplier = 1.0 + (0.3 * level);
            cir.setReturnValue(cir.getReturnValue() * multiplier);
        }
    }
}
