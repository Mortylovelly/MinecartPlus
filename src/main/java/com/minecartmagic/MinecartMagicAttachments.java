package com.minecartmagic;

/**
 * Compatibility holder for the 1.20.1 port.
 *
 * The attachment registration used by the 1.21.1 branch is not used on this
 * 1.20.1 port. Enchantment levels are synchronized through dedicated
 * DataTracker values and persisted through the existing entity command-tag
 * fallback in ModEnchantments.
 */
public final class MinecartMagicAttachments {

    private MinecartMagicAttachments() {
    }

    public static void init() {
        // No attachment registration is required on Minecraft 1.20.1.
    }
}
