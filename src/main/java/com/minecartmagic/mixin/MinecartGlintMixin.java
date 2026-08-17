package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public abstract class MinecartGlintMixin<T extends AbstractMinecartEntity> {

    @Shadow
    protected EntityModel<T> model;

    @Inject(
            method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                    ordinal = 0
            )
    )
    private void minecartmagic$renderTractionGlint(
            T minecart,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (ModEnchantments.getTractionLevel(minecart.getPickBlockStack()) <= 0) {
            return;
        }

        VertexConsumer glintConsumer =
                vertexConsumers.getBuffer(RenderLayer.getEntityGlint());

        model.render(matrices, glintConsumer, light, 0, 0xFFFFFFFF);
    }
}
