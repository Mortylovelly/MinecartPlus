package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({AbstractMinecartEntity.class, StorageMinecartEntity.class, TntMinecartEntity.class})
public abstract class MinecartDropItemMixin {

    @Redirect(
            method = "dropItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;dropStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;"
            )
    )
    private ItemEntity minecartmagic$enchantDroppedMinecart(
            AbstractMinecartEntity minecart,
            ItemStack stack
    ) {
        int level = ModEnchantments.getTractionLevel(minecart);

        if (level <= 0 || stack.isEmpty() || stack.getItem() != minecart.getItem()) {
            return minecart.dropStack(stack);
        }

        Registry<Enchantment> enchantmentRegistry =
                minecart.getWorld().getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> traction =
                enchantmentRegistry
                        .getEntry(ModEnchantments.TRACTION_KEY)
                        .orElse(null);

        if (traction != null) {
            stack.addEnchantment(
                    traction.value(),
                    level
            );
        }

        return minecart.dropStack(stack);
    }
}
