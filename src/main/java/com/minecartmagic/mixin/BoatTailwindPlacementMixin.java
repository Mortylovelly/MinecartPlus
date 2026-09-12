package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public abstract class BoatTailwindPlacementMixin {

    @Unique
    private static final ThreadLocal<ItemStack> MINECART_MAGIC$PLACEMENT_STACK =
            new ThreadLocal<>();

    @Shadow
    private abstract BoatEntity createEntity(
            World world,
            HitResult hitResult
    );

    @Inject(
            method = "use",
            at = @At("HEAD")
    )
    private void minecartmagic$capturePlacementStack(
            World world,
            PlayerEntity user,
            Hand hand,
            CallbackInfoReturnable<?> cir
    ) {
        MINECART_MAGIC$PLACEMENT_STACK.set(user.getStackInHand(hand));
    }

    @Inject(
            method = "use",
            at = @At("RETURN")
    )
    private void minecartmagic$clearPlacementStack(
            World world,
            PlayerEntity user,
            Hand hand,
            CallbackInfoReturnable<?> cir
    ) {
        MINECART_MAGIC$PLACEMENT_STACK.remove();
    }

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
            HitResult hitResult
    ) {
        BoatEntity boat = this.createEntity(world, hitResult);

        if (boat == null) {
            return null;
        }

        ItemStack stack = MINECART_MAGIC$PLACEMENT_STACK.get();

        if (stack != null) {
            int level = ModEnchantments.getTailwindLevel(stack);

            if (level > 0) {
                ModEnchantments.setTailwindLevel(boat, level);
            }
        }

        return boat;
    }
}
