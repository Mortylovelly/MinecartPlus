package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.MinecartPlacementHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntity.class)
public abstract class MinecartDropMixin {

    @Inject(
            method = "dropItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$dropEnchantedMinecart(
            DamageSource source,
            CallbackInfo ci
    ) {
        MinecartEntity minecart =
                (MinecartEntity) (Object) this;

        int level = 0;

        /*
         * Определяем уровень Тяги,
         * сохранённый на вагонетке.
         */
        for (int i = 3; i >= 1; i--) {
            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(i)
            )) {
                level = i;
                break;
            }
        }

        /*
         * Обычная вагонетка.
         * Оставляем ванильное выпадение.
         */
        if (level <= 0) {
            return;
        }

        /*
         * Получаем реестр зачарований.
         */
        Registry<Enchantment> enchantmentRegistry =
                minecart.getWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        /*
         * Получаем настоящее зачарование Тяга.
         */
        RegistryEntry<Enchantment> traction =
                enchantmentRegistry
                        .getEntry(ModEnchantments.TRACTION_KEY)
                        .orElse(null);

        if (traction == null) {
            return;
        }

        /*
         * Создаём ItemStack обычной вагонетки.
         */
        ItemStack stack = new ItemStack(Items.MINECART);

        /*
         * Берём текущие зачарования предмета.
         */
        ItemEnchantmentsComponent currentEnchantments =
                stack.getOrDefault(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT
                );

        ItemEnchantmentsComponent.Builder enchantments =
                new ItemEnchantmentsComponent.Builder(
                        currentEnchantments
                );

        /*
         * Добавляем настоящее Тяга.
         */
        enchantments.set(
                traction,
                level
        );

        stack.set(
                DataComponentTypes.ENCHANTMENTS,
                enchantments.build()
        );

        /*
         * Выбрасываем зачарованную вагонетку.
         */
        minecart.dropStack(stack);

        /*
         * Не даём ванильному коду выбросить
         * ещё одну обычную вагонетку.
         */
        ci.cancel();
    }
}
