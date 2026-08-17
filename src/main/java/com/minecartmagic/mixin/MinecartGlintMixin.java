package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public class MinecartGlintMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void minecartmagic$renderTractionGlint(
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        int tractionLevel = ModEnchantments.getTractionLevel(
                minecart.getPickBlockStack()
        );

        if (tractionLevel <= 0) {
            return;
        }

        // Пока только проверяем корректное подключение Mixin.
        // Рендер самого glint добавим следующим безопасным этапом.
    }
}
