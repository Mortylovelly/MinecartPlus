package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({BoatEntity.class, ChestBoatEntity.class})
public abstract class BoatTailwindDropMixin {

    @Redirect(
            method = "dropItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;dropStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;"
            )
    )
    private ItemEntity minecartmagic$enchantDroppedBoat(
            BoatEntity boat,
            ItemStack stack
    ) {
        int level = ModEnchantments.getTailwindLevel(boat);

        if (level <= 0 || stack.isEmpty() || stack.getItem() != boat.asItem()) {
            return boat.dropStack(stack);
        }

        Registry<Enchantment> enchantmentRegistry =
                boat.getWorld().getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> tailwind =
                enchantmentRegistry
                        .getEntry(ModEnchantments.TAILWIND_KEY)
                        .orElse(null);

        if (tailwind != null) {
            stack.addEnchantment(
                    tailwind.value(),
                    level
            );
        }

        return boat.dropStack(stack);
    }
}
