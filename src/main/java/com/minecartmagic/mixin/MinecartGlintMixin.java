package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecartEntityRenderer.class)
public abstract class MinecartGlintMixin {

    private static final Identifier MINECART_TEXTURE =
            Identifier.ofVanilla("textures/entity/minecart.png");

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"
            ),
            index = 1
    )
    private VertexConsumer minecartmagic$applyTractionGlint(
            VertexConsumer original,
            MatrixStack matrices,
            VertexConsumer originalConsumer,
            int light,
            int overlay
    ) {
        return ItemRenderer.getDirectItemGlintConsumer(
                MinecartGlintMixin$Holder.vertexConsumers,
                RenderLayer.getEntityCutoutNoCull(MINECART_TEXTURE),
                true,
                true
        );
    }

    private static class MinecartGlintMixin$Holder {
        private static VertexConsumerProvider vertexConsumers;
    }
}
