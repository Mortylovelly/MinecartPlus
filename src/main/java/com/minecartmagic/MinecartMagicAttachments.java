package com.minecartmagic;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class MinecartMagicAttachments {

    public static final AttachmentType<Integer> TRACTION_LEVEL =
            AttachmentRegistry.create(
                    MinecartMagicMod.id("traction_level"),
                    builder -> builder
                            .initializer(() -> 0)
                            .persistent(Codec.INT)
                            .syncable()
            );

    public static final AttachmentType<Integer> TAILWIND_LEVEL =
            AttachmentRegistry.create(
                    MinecartMagicMod.id("tailwind_level"),
                    builder -> builder
                            .initializer(() -> 0)
                            .persistent(Codec.INT)
                            .syncable()
            );

    private MinecartMagicAttachments() {
    }
}
