package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(MinecartMagicMod.MOD_ID, "traction")
    );

    private ModEnchantments() {
    }

    public static void init() {
        // Enchantments are data-driven since Minecraft 1.21.
        // The actual enchantment is defined in:
        // data/minecartmagic/enchantment/traction.json
    }
}
