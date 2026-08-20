package com.minecartmagic;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    Identifier.of(
                            MinecartMagicMod.MOD_ID,
                            "traction"
                    )
            );

    public static final RegistryKey<Enchantment> TAILWIND_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    Identifier.of(
                            MinecartMagicMod.MOD_ID,
                            "tailwind"
                    )
            );

    private static final String TRACTION_TAG_PREFIX =
            "minecartmagic_traction_";

    private static final String TAILWIND_TAG_PREFIX =
            "minecartmagic_tailwind_";

    private ModEnchantments() {
    }

    public static void init() {
    }

    private static int getItemEnchantmentLevel(
            ItemStack stack,
            RegistryKey<Enchantment> key
    ) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        for (var entry :
                EnchantmentHelper.getEnchantments(stack)
                        .getEnchantmentEntries()) {

            RegistryEntry<Enchantment> enchantment =
                    entry.getKey();

            if (enchantment.getKey().isPresent()
                    && enchantment.getKey().get().equals(key)) {

                return EnchantmentHelper.getLevel(
                        enchantment,
                        stack
                );
            }
        }

        return 0;
    }

    public static int getTractionLevel(
            ItemStack stack
    ) {
        return getItemEnchantmentLevel(
                stack,
                TRACTION_KEY
        );
    }

    public static int getTailwindLevel(
            ItemStack stack
    ) {
        return getItemEnchantmentLevel(
                stack,
                TAILWIND_KEY
        );
    }

    public static int getTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        Integer level =
                ((AttachmentTarget) minecart).getAttached(
                        MinecartMagicAttachments.TRACTION_LEVEL
                );

        if (level != null && level > 0) {
            return level;
        }

        for (String tag : minecart.getCommandTags()) {

            if (!tag.startsWith(
                    TRACTION_TAG_PREFIX
            )) {
                continue;
            }

            try {
                return Integer.parseInt(
                        tag.substring(
                                TRACTION_TAG_PREFIX.length()
                        )
                );
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    public static int getTractionLevel(
            MinecartEntity minecart
    ) {
        return getTractionLevel(
                (AbstractMinecartEntity) minecart
        );
    }

    public static void setTractionLevel(
            AbstractMinecartEntity minecart,
            int level
    ) {
        int safeLevel =
                Math.max(0, level);

        ((AttachmentTarget) minecart).setAttached(
                MinecartMagicAttachments.TRACTION_LEVEL,
                safeLevel
        );

        minecart.getCommandTags().removeIf(
                tag -> tag.startsWith(
                        TRACTION_TAG_PREFIX
                )
        );

        if (safeLevel > 0) {
            minecart.addCommandTag(
                    TRACTION_TAG_PREFIX + safeLevel
            );
        }
    }

    public static void setTractionLevel(
            MinecartEntity minecart,
            int level
    ) {
        setTractionLevel(
                (AbstractMinecartEntity) minecart,
                level
        );
    }

    public static int getTailwindLevel(
            BoatEntity boat
    ) {
        Integer level =
                ((AttachmentTarget) boat).getAttached(
                        MinecartMagicAttachments.TAILWIND_LEVEL
                );

        if (level != null && level > 0) {
            return level;
        }

        for (String tag : boat.getCommandTags()) {

            if (!tag.startsWith(
                    TAILWIND_TAG_PREFIX
            )) {
                continue;
            }

            try {
                int tagLevel =
                        Integer.parseInt(
                                tag.substring(
                                        TAILWIND_TAG_PREFIX.length()
                                )
                        );

                if (tagLevel > 0) {
                    return tagLevel;
                }

            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    public static void setTailwindLevel(
            BoatEntity boat,
            int level
    ) {
        int safeLevel =
                Math.max(0, level);

        ((AttachmentTarget) boat).setAttached(
                MinecartMagicAttachments.TAILWIND_LEVEL,
                safeLevel
        );

        boat.getCommandTags().removeIf(
                tag -> tag.startsWith(
                        TAILWIND_TAG_PREFIX
                )
        );

        if (safeLevel > 0) {
            boat.addCommandTag(
                    TAILWIND_TAG_PREFIX + safeLevel
            );
        }
    }

    public static String getTractionTag(
            int level
    ) {
        return TRACTION_TAG_PREFIX + level;
    }

    public static String getTailwindTag(
            int level
    ) {
        return TAILWIND_TAG_PREFIX + level;
    }
}
