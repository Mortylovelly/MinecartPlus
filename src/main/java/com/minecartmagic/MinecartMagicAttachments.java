package com.minecartmagic;

/**
 * Compatibility holder for the 1.20.1 port.
 *
 * The Fabric Attachment API used by the 1.21.1 branch does not exist in the
 * 1.20.1 Fabric API version used here. Minecart/boat enchantment state is
 * stored through the existing entity command-tag fallback in ModEnchantments.
 */
public final class MinecartMagicAttachments {

    private MinecartMagicAttachments() {
    }

    public static void init() {
        // No registration is required on Minecraft 1.20.1.
    }
}
