package com.minecartmagic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minecartmagic.ModEnchantments;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecartEntityRenderer.class)
public abstract class MinecartGlintMixin {

    @WrapOperation(
            method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"
            )
    )
    private void minecartmagic$addTractionGlint(
            Model model,
            MatrixStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color,
            Operation<Void> original,
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            MatrixStack renderMatrices,
            VertexConsumerProvider vertexConsumers,
            int renderLight
    ) {
        // Сначала полностью выполняем обычный ванильный рендер.
        original.call(model, matrices, vertices, light, overlay, color);

        // Получаем реальный уровень Тяги из существующей системы зачарования.
        int tractionLevel = ModEnchantments.getTractionLevel(
                minecart.getPickBlockStack()
        );

        if (tractionLevel <= 0) {
            return;
        }

        // Повторно рисуем ту же модель ванильным entity enchantment glint.
        VertexConsumer glintConsumer =
                vertexConsumers.getBuffer(RenderLayer.getEntityGlint());

        model.render(
                matrices,
                glintConsumer,
                light,
                overlay,
                color
        );
    }
}
