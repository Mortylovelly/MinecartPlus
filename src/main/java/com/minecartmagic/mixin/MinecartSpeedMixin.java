package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartSpeedMixin {

    @Unique
    private int minecartmagic$tractionLevel = 0;

    @Inject(
        method = "create",
        at = @At("RETURN")
    )
    private static void minecartmagic$copyTractionFromItem(
        ServerWorld world,
        double x,
        double y,
        double z,
        AbstractMinecartEntity.Type type,
        ItemStack stack,
        @Nullable PlayerEntity player,
        CallbackInfoReturnable<AbstractMinecartEntity> cir
    ) {
        AbstractMinecartEntity minecart = cir.getReturnValue();

        if (minecart == null) {
            return;
        }

        if (stack.getItem() != Items.MINECART) {
            return;
        }

        int level = ModEnchantments.getTractionLevel(stack);

        if (level > 0) {
            MinecartSpeedMixin access = (MinecartSpeedMixin) (Object) minecart;
            access.minecartmagic$setTractionLevel(level);
        }
    }

    @Inject(
        method = "getMaxSpeed",
        at = @At("RETURN"),
        cancellable = true
    )
    private void minecartmagic$modifyMaxSpeed(
        ServerWorld world,
        CallbackInfoReturnable<Double> cir
    ) {
        int level = minecartmagic$tractionLevel;

        if (level <= 0) {
            return;
        }

        double baseSpeed = cir.getReturnValue();
        double multiplier = 1.0 + (level * 0.3);

        cir.setReturnValue(baseSpeed * multiplier);
    }

    @Unique
    private void minecartmagic$setTractionLevel(int level) {
        minecartmagic$tractionLevel = Math.max(0, Math.min(3, level));

        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        minecart.setGlowing(minecartmagic$tractionLevel > 0);
    }
}
