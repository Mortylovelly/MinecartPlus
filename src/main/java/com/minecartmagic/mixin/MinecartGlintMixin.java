package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartEntityRenderer.class)
public abstract class MinecartGlintMixin {

    @Shadow
    protected EntityModel<AbstractMinecartEntity> model;

    @Shadow
    public abstract Identifier getTexture(AbstractMinecartEntity minecart);

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"
            )
    )
    private void minecartmagic$renderWithTractionGlint(
            EntityModel<AbstractMinecartEntity> model,
            MatrixStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            MatrixStack renderMatrices,
            VertexConsumerProvider vertexConsumers,
            int renderLight
    ) {
        model.render(matrices, vertices, light, overlay);

        if (ModEnchantments.getTractionLevel(minecart.getPickBlockStack()) <= 0) {
            return;
        }

        Identifier texture = getTexture(minecart);
        RenderLayer baseLayer = model.getLayer(texture);

        VertexConsumer glint = ItemRenderer.getDirectItemGlintConsumer(
                vertexConsumers,
                baseLayer,
                true,
                true
        );

        model.render(matrices, glint, light, OverlayTexture.DEFAULT_UV);
    }
}
