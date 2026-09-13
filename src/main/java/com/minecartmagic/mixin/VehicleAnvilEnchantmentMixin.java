package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class VehicleAnvilEnchantmentMixin {

    @Shadow @Final private Property levelCost;

    @Shadow private int repairItemUsage;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void minecartmagic$updateVehicleEnchantmentResult(
            CallbackInfo ci
    ) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        ItemStack target = handler.getSlot(0).getStack();
        ItemStack book = handler.getSlot(1).getStack();

        if (target.isEmpty() || book.isEmpty()
                || !book.isOf(Items.ENCHANTED_BOOK)) {
            return;
        }

        Enchantment enchantment = minecartmagic$getVehicleEnchantment(book);
        if (enchantment == null) {
            return;
        }

        if (!minecartmagic$isValidTarget(target, enchantment)) {
            return;
        }

        int bookLevel = EnchantmentHelper.getLevel(enchantment, book);
        if (bookLevel <= 0) {
            return;
        }

        int currentLevel = EnchantmentHelper.getLevel(enchantment, target);
        int resultLevel = bookLevel;

        if (currentLevel > resultLevel) {
            resultLevel = currentLevel;
        } else if (currentLevel == resultLevel && resultLevel < enchantment.getMaxLevel()) {
            resultLevel++;
        }

        ItemStack result = target.copy();
        result.addEnchantment(enchantment, Math.min(resultLevel, enchantment.getMaxLevel()));

        handler.getSlot(2).setStack(result);
        levelCost.set(1);
        repairItemUsage = 1;
        ci.cancel();
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void minecartmagic$takeVehicleEnchantmentOutput(
            PlayerEntity player,
            ItemStack stack,
            CallbackInfo ci
    ) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        ItemStack target = handler.getSlot(0).getStack();
        ItemStack book = handler.getSlot(1).getStack();

        if (target.isEmpty() || book.isEmpty()
                || !book.isOf(Items.ENCHANTED_BOOK)
                || minecartmagic$getVehicleEnchantment(book) == null
                || !minecartmagic$isValidTarget(
                        target,
                        minecartmagic$getVehicleEnchantment(book)
                )) {
            return;
        }

        handler.getSlot(0).setStack(ItemStack.EMPTY);
        handler.getSlot(1).setStack(ItemStack.EMPTY);

        if (!player.isCreative()) {
            player.addExperienceLevels(-1);
        }

        ci.cancel();
    }

    private static Enchantment minecartmagic$getVehicleEnchantment(
            ItemStack book
    ) {
        Enchantment traction = minecartmagic$getEnchantment(
                book,
                ModEnchantments.TRACTION_KEY.getValue()
        );
        if (traction != null) {
            return traction;
        }

        return minecartmagic$getEnchantment(
                book,
                ModEnchantments.TAILWIND_KEY.getValue()
        );
    }

    private static Enchantment minecartmagic$getEnchantment(
            ItemStack book,
            Identifier id
    ) {
        for (Enchantment enchantment : EnchantmentHelper.get(book).keySet()) {
            Identifier enchantmentId = EnchantmentHelper.getEnchantmentId(enchantment);
            if (id.equals(enchantmentId)) {
                return enchantment;
            }
        }
        return null;
    }

    private static boolean minecartmagic$isValidTarget(
            ItemStack stack,
            Enchantment enchantment
    ) {
        Identifier id = EnchantmentHelper.getEnchantmentId(enchantment);

        if (ModEnchantments.TRACTION_KEY.getValue().equals(id)) {
            return stack.isOf(Items.MINECART)
                    || stack.isOf(Items.CHEST_MINECART)
                    || stack.isOf(Items.FURNACE_MINECART)
                    || stack.isOf(Items.TNT_MINECART)
                    || stack.isOf(Items.HOPPER_MINECART)
                    || stack.isOf(Items.COMMAND_BLOCK_MINECART)
                    || stack.isOf(ModItems.ADVANCED_MINECART);
        }

        if (ModEnchantments.TAILWIND_KEY.getValue().equals(id)) {
            Item item = stack.getItem();
            return item instanceof BoatItem
                    || item == Items.OAK_CHEST_BOAT
                    || item == Items.SPRUCE_CHEST_BOAT
                    || item == Items.BIRCH_CHEST_BOAT
                    || item == Items.JUNGLE_CHEST_BOAT
                    || item == Items.ACACIA_CHEST_BOAT
                    || item == Items.DARK_OAK_CHEST_BOAT
                    || item == Items.MANGROVE_CHEST_BOAT
                    || item == Items.CHERRY_CHEST_BOAT
                    || item == Items.BAMBOO_CHEST_RAFT
                    || item == ModItems.SELF_PROPELLING_BOAT
                    || item == ModItems.SELF_PROPELLING_SPRUCE_BOAT
                    || item == ModItems.SELF_PROPELLING_BIRCH_BOAT
                    || item == ModItems.SELF_PROPELLING_JUNGLE_BOAT
                    || item == ModItems.SELF_PROPELLING_ACACIA_BOAT
                    || item == ModItems.SELF_PROPELLING_DARK_OAK_BOAT
                    || item == ModItems.SELF_PROPELLING_MANGROVE_BOAT
                    || item == ModItems.SELF_PROPELLING_CHERRY_BOAT
                    || item == ModItems.SELF_PROPELLING_BAMBOO_RAFT;
        }

        return false;
    }
}
