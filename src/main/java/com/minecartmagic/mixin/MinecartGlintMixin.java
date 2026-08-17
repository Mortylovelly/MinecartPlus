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
public abstract class MinecartGlintMixin {

    @Shadow
    protected EntityModel<AbstractMinecartEntity> model;

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void minecartmagic$renderTractionGlint(
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (!(minecart instanceof net.minecraft.entity.vehicle.MinecartEntity normalMinecart)) {
            return;
        }

        int tractionLevel = ModEnchantments.getTractionLevel(normalMinecart);

        if (tractionLevel <= 0) {
            return;
        }

        VertexConsumer glint = vertexConsumers.getBuffer(
                RenderLayer.getDirectEntityGlint()
        );

        model.render(
                matrices,
                glint,
                light,
                0
        );
    }
}
