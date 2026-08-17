package com.minecartmagic.mixin;

import com.minecartmagic.MinecartPlacementHandler;
import com.minecartmagic.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        /*
         * Работаем только с обычной вагонеткой.
         *
         * Chest minecart, furnace minecart и остальные
         * типы вагонеток ванильный дроп не затрагиваем.
         */
        if (!((Object) this instanceof MinecartEntity minecart)) {
            return;
        }

        /*
         * Определяем уровень Тяги по command tag.
         */
        int level = 0;

        for (int i = 3; i >= 1; i--) {
            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(i)
            )) {
                level = i;
                break;
            }
        }

        /*
         * Если Тяги нет — полностью оставляем ванильный дроп.
         */
        if (level <= 0) {
            return;
        }

        /*
         * Получаем реестр зачарований текущего мира.
         */
        Registry<Enchantment> enchantmentRegistry =
                minecart.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

        /*
         * Получаем зарегистрированную Тягу.
         */
        RegistryEntry<Enchantment> traction =
                enchantmentRegistry
                        .getEntry(ModEnchantments.TRACTION_KEY)
                        .orElse(null);

        /*
         * Если зачарование почему-то не найдено,
         * не ломаем ванильный дроп.
         */
        if (traction == null) {
            return;
        }

        /*
         * Создаём обычную вагонетку.
         */
        ItemStack stack = new ItemStack(Items.MINECART);

        /*
         * Накладываем на неё настоящее зачарование Тяга.
         */
        stack.addEnchantment(traction, level);

        /*
         * Выбрасываем зачарованную вагонетку.
         */
        minecart.dropStack(stack);

        /*
         * Отменяем ванильный killAndDropItem(),
         * чтобы обычная вагонетка дополнительно не выпала.
         */
        minecart.discard();
        ci.cancel();
    }
}
