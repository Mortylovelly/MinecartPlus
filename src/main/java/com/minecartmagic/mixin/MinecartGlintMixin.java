package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartEntityRenderer.class)
public class MinecartGlintMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private VertexConsumer minecartmagic$useTractionGlint(
            VertexConsumerProvider vertexConsumers,
            RenderLayer originalLayer,
            AbstractMinecartEntity minecart
    ) {
        if (minecart instanceof MinecartEntity normalMinecart) {
            int tractionLevel = ModEnchantments.getTractionLevel(
                    normalMinecart
            );

            if (tractionLevel > 0) {
                return vertexConsumers.getBuffer(
                        RenderLayer.getDirectEntityGlint()
                );
            }
        }

        return vertexConsumers.getBuffer(originalLayer);
    }
}
