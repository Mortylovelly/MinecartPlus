package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public abstract class BoatTailwindPlacementMixin {

    @Inject(
            method = "createEntity",
            at = @At("RETURN")
    )
    private void minecartmagic$applyTailwind(
            World world,
            HitResult hitResult,
            ItemStack stack,
            PlayerEntity player,
            CallbackInfoReturnable<BoatEntity> cir
    ) {
        BoatEntity boat = cir.getReturnValue();

        if (boat == null) {
            return;
        }

        int level =
                ModEnchantments.getTailwindLevel(stack);

        if (level <= 0) {
            return;
        }

        ModEnchantments.setTailwindLevel(
                boat,
                level
        );
    }
}
