package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
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
public abstract class MinecartDropItemMixin {

    @Inject(
            method = "killAndDropItem(Lnet/minecraft/item/Item;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$dropEnchantedMinecart(
            Item selfAsItem,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof AbstractMinecartEntity minecart)) {
            return;
        }

        int level =
                ModEnchantments.getTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry =
                minecart.getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> traction =
                enchantmentRegistry
                        .getEntry(ModEnchantments.TRACTION_KEY)
                        .orElse(null);

        if (traction == null) {
            return;
        }

        /*
         * selfAsItem — именно тот предмет,
         * который соответствует типу этой вагонетки.
         *
         * Поэтому:
         * Chest Minecart -> Chest Minecart
         * Hopper Minecart -> Hopper Minecart
         * Furnace Minecart -> Furnace Minecart
         * TNT Minecart -> TNT Minecart
         * и т.д.
         */
        ItemStack stack =
                new ItemStack(selfAsItem);

        stack.addEnchantment(
                traction,
                level
        );

        minecart.dropStack(stack);

        minecart.discard();

        ci.cancel();
    }
}
