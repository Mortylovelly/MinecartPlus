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
        MinecartEntity minecart = (MinecartEntity) (Object) this;

        int level = 0;

        for (int i = 3; i >= 1; i--) {
            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(i)
            )) {
                level = i;
                break;
            }
        }

        if (level <= 0) {
            return;
        }

        /*
         * Получаем настоящее зачарование из динамического
         * реестра Minecraft 1.21.1.
         */
        var enchantmentRegistry =
                minecart.getWorld()
                        .getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> traction =
                enchantmentRegistry
                        .getOrThrow(ModEnchantments.TRACTION);

        /*
         * Создаём обычную вагонетку как ItemStack.
         */
        ItemStack stack = new ItemStack(Items.MINECART);

        /*
         * Накладываем настоящее зачарование Тяга.
         */
        ItemEnchantmentsComponent.Builder enchantments =
                new ItemEnchantmentsComponent.Builder(
                        stack.getOrDefault(
                                DataComponentTypes.ENCHANTMENTS,
                                ItemEnchantmentsComponent.DEFAULT
                        )
                );

        enchantments.set(
                traction,
                level
        );

        stack.set(
                DataComponentTypes.ENCHANTMENTS,
                enchantments.build()
        );

        /*
         * Выбрасываем именно зачарованную вагонетку.
         *
         * Ванильный dropItems больше не выполняется,
         * поэтому второй обычной вагонетки не будет.
         */
        minecart.dropStack(stack);

        ci.cancel();
    }
}
