package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatItem.class)
public abstract class BoatTailwindPlacementMixin {

    @Shadow
    private abstract BoatEntity createEntity(
            World world,
            HitResult hitResult
    );

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BoatItem;createEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/hit/HitResult;)Lnet/minecraft/entity/vehicle/BoatEntity;"
            )
    )
    private BoatEntity minecartmagic$createTailwindBoat(
            BoatItem self,
            World world,
            HitResult hitResult,
            ItemStack stack
    ) {
        BoatEntity boat = this.createEntity(world, hitResult);

        if (boat == null) {
            return null;
        }

        int level = ModEnchantments.getTailwindLevel(stack);

        if (level > 0) {
            ModEnchantments.setTailwindLevel(boat, level);
        }

        return boat;
    }
}
