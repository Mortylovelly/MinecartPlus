package com.minecartmagic.mixin;

import com.minecartmagic.MinecartPlacementHandler;
import net.minecraft.client.model.Model;
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
                    target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                    shift = At.Shift.AFTER
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
        if (minecart == null || !minecart.isAlive()) {
            return;
        }

        int tractionLevel = 0;

        for (int level = 3; level >= 1; level--) {
            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(level)
            )) {
                tractionLevel = level;
                break;
            }
        }

        if (tractionLevel <= 0) {
            return;
        }

        VertexConsumer glintConsumer =
                vertexConsumers.getBuffer(RenderLayer.getEntityGlint());

        this.model.render(
                matrices,
                glintConsumer,
                light,
                0,
                0xFFFFFFFF
        );
    }
}
