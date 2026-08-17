package com.minecartmagic;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.PacketCodecs;

public final class MinecartMagicAttachments {

    public static final AttachmentType<Integer> TRACTION_LEVEL =
            AttachmentRegistry.create(
                    MinecartMagicMod.id("traction_level"),
                    builder -> builder
                            .initializer(() -> 0)
                            .persistent(com.mojang.serialization.Codec.INT)
                            .syncWith(
                                    PacketCodecs.VAR_INT,
                                    AttachmentSyncPredicate.all()
                            )
            );

    public static final AttachmentType<Integer> TAILWIND_LEVEL =
            AttachmentRegistry.create(
                    MinecartMagicMod.id("tailwind_level"),
                    builder -> builder
                            .initializer(() -> 0)
                            .persistent(com.mojang.serialization.Codec.INT)
                            .syncWith(
                                    PacketCodecs.VAR_INT,
                                    AttachmentSyncPredicate.all()
                            )
            );

    private MinecartMagicAttachments() {
    }
}
