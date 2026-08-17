package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public class MinecartGlintMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
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
        int tractionLevel =
                ModEnchantments.getTractionLevel(minecart);

        if (tractionLevel <= 0) {
            return;
        }

        // Glint будет добавлен следующим безопасным способом.
        // Здесь намеренно пока ничего не рендерим.
    }
}
