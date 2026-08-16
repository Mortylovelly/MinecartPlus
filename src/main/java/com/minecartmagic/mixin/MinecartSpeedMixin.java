```java
package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
            World world,
            double x,
            double y,
            double z,
            EntityType<?> type,
            SpawnReason reason,
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

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void minecartmagic$readTraction(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        if (!nbt.contains("MinecartMagicTraction")) {
            minecartmagic$setTractionLevel(0);
            return;
        }

        int level = nbt.getInt("MinecartMagicTraction");

        if (level < 0) {
            level = 0;
        }

        if (level > 3) {
            level = 3;
        }

        minecartmagic$setTractionLevel(level);
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void minecartmagic$writeTraction(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        if (minecartmagic$tractionLevel > 0) {
            nbt.putInt("MinecartMagicTraction", minecartmagic$tractionLevel);
        }
    }

    @Unique
    private void minecartmagic$setTractionLevel(int level) {
        minecartmagic$tractionLevel = Math.max(0, Math.min(3, level));

        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        minecart.setGlowing(minecartmagic$tractionLevel > 0);
    }
}
```
