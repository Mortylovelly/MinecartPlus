package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
        AbstractMinecartEntity.class,
        StorageMinecartEntity.class,
        TntMinecartEntity.class
})
public abstract class MinecartDropItemMixin {

    @Redirect(
            method = "dropItems(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"
            ),
            require = 0
    )
    private ItemEntity minecartmagic$enchantDroppedMinecartFromAbstract(
            AbstractMinecartEntity minecart,
            ItemConvertible item
    ) {
        return minecartmagic$dropEnchanted(minecart, item);
    }

    @Redirect(
            method = "dropItems(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/vehicle/StorageMinecartEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"
            ),
            require = 0
    )
    private ItemEntity minecartmagic$enchantDroppedMinecartFromStorage(
            StorageMinecartEntity minecart,
            ItemConvertible item
    ) {
        return minecartmagic$dropEnchanted(minecart, item);
    }

    @Redirect(
            method = "dropItems(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/vehicle/TntMinecartEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"
            ),
            require = 0
    )
    private ItemEntity minecartmagic$enchantDroppedMinecartFromTnt(
            TntMinecartEntity minecart,
            ItemConvertible item
    ) {
        return minecartmagic$dropEnchanted(minecart, item);
    }

    private static ItemEntity minecartmagic$dropEnchanted(
            AbstractMinecartEntity minecart,
            ItemConvertible item
    ) {
        ItemStack stack = new ItemStack(item.asItem());
        int level = ModEnchantments.getTractionLevel(minecart);

        if (level > 0) {
            Registry<Enchantment> enchantmentRegistry =
                    minecart.getWorld().getRegistryManager()
                            .get(RegistryKeys.ENCHANTMENT);
            RegistryEntry<Enchantment> traction =
                    enchantmentRegistry
                            .getEntry(ModEnchantments.TRACTION_KEY)
                            .orElse(null);

            if (traction != null) {
                stack.addEnchantment(traction.value(), level);
            }
        }

        return minecart.dropStack(stack);
    }
}
