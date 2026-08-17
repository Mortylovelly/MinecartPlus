package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VehicleEntity.class)
public abstract class BoatTailwindDropMixin {

    @Inject(
            method = "killAndDropItem(Lnet/minecraft/item/Item;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$dropTailwindBoat(
            Item selfAsItem,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof BoatEntity boat)) {
            return;
        }

        int level =
                ModEnchantments.getTailwindLevel(boat);

        if (level <= 0) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry =
                boat.getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> tailwind =
                enchantmentRegistry
                        .getEntry(
                                ModEnchantments.TAILWIND_KEY
                        )
                        .orElse(null);

        if (tailwind == null) {
            return;
        }

        ItemStack stack =
                new ItemStack(selfAsItem);

        stack.addEnchantment(
                tailwind,
                level
        );

        boat.dropStack(stack);

        boat.discard();

        ci.cancel();
    }
}
