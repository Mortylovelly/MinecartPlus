package com.minecartmagic;

import com.minecartmagic.mixin.BoatTailwindDataAccess;
import com.minecartmagic.mixin.MinecartTractionDataAccess;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    new Identifier(
                            MinecartMagicMod.MOD_ID,
                            "traction"
                    )
            );

    public static final RegistryKey<Enchantment> TAILWIND_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    new Identifier(
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

        Identifier targetId = key.getValue();

        for (var entry : EnchantmentHelper.get(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            Identifier enchantmentId =
                    EnchantmentHelper.getEnchantmentId(enchantment);

            if (targetId.equals(enchantmentId)) {
                return entry.getValue();
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
        if (minecart instanceof MinecartTractionDataAccess dataAccess) {
            int syncedLevel =
                    dataAccess.minecartmagic$getTractionDataLevel();

            if (syncedLevel > 0) {
                return syncedLevel;
            }
        }

        for (String tag : minecart.getCommandTags()) {

            if (!tag.startsWith(
                    TRACTION_TAG_PREFIX
            )) {
                continue;
            }

            try {
                int level = Integer.parseInt(
                        tag.substring(
                                TRACTION_TAG_PREFIX.length()
                        )
                );

                if (level > 0
                        && minecart instanceof MinecartTractionDataAccess dataAccess) {
                    dataAccess.minecartmagic$setTractionDataLevel(level);
                }

                return level;
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

        if (minecart instanceof MinecartTractionDataAccess dataAccess) {
            dataAccess.minecartmagic$setTractionDataLevel(safeLevel);
        }

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
        if (boat instanceof BoatTailwindDataAccess dataAccess) {
            int syncedLevel =
                    dataAccess.minecartmagic$getTailwindDataLevel();

            if (syncedLevel > 0) {
                return syncedLevel;
            }
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
                    if (boat instanceof BoatTailwindDataAccess dataAccess) {
                        dataAccess.minecartmagic$setTailwindDataLevel(tagLevel);
                    }

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

        if (boat instanceof BoatTailwindDataAccess dataAccess) {
            dataAccess.minecartmagic$setTailwindDataLevel(safeLevel);
        }

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
