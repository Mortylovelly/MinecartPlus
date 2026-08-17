package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public abstract class MinecartGlintMixin {

    @Unique
    private static final ThreadLocal<AbstractMinecartEntity> MINECART_MAGIC$CURRENT_MINECART =
            new ThreadLocal<>();

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void minecartmagic$beginRender(
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            net.minecraft.client.util.math.MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MINECART_MAGIC$CURRENT_MINECART.set(minecart);
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void minecartmagic$endRender(
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            net.minecraft.client.util.math.MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MINECART_MAGIC$CURRENT_MINECART.remove();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private VertexConsumer minecartmagic$addGlint(
            VertexConsumerProvider vertexConsumers,
            RenderLayer layer
    ) {
        AbstractMinecartEntity minecart = MINECART_MAGIC$CURRENT_MINECART.get();

        if (minecart != null
                && ModEnchantments.getTractionLevel(minecart) > 0) {

            return ItemRenderer.getDirectItemGlintConsumer(
                    vertexConsumers,
                    layer,
                    false,
                    true
            );
        }

        return vertexConsumers.getBuffer(layer);
    }
}
