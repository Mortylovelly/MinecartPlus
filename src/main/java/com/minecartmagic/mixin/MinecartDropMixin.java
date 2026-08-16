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
         * Получаем реестр зачарований через getRegistryManager().
         * В этой версии mappings нужен обычный get().
         */
        var enchantmentRegistry =
                minecart.getWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT);

        if (enchantmentRegistry.isEmpty()) {
            return;
        }

        RegistryEntry<Enchantment> traction =
                enchantmentRegistry.get()
                        .getEntry(ModEnchantments.TRACTION_KEY)
                        .orElse(null);

        if (traction == null) {
            return;
        }

        /*
         * Создаём обычную вагонетку.
         */
        ItemStack stack = new ItemStack(Items.MINECART);

        /*
         * Добавляем настоящее зачарование Тяга.
         */
        ItemEnchantmentsComponent.Builder enchantments =
                new ItemEnchantmentsComponent.Builder(
                        stack.getOrDefault(
                                DataComponentTypes.ENCHANTMENTS,
                                ItemEnchantmentsComponent.DEFAULT
                        )
                );

        enchantments.set(traction, level);

        stack.set(
                DataComponentTypes.ENCHANTMENTS,
                enchantments.build()
        );

        /*
         * Выбрасываем именно зачарованную вагонетку.
         */
        minecart.dropStack(stack);

        /*
         * Запрещаем ванильному dropItems()
         * дополнительно выбросить обычную вагонетку.
         */
        ci.cancel();
    }
}
