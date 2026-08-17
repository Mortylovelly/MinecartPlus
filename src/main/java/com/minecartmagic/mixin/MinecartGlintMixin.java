package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public class MinecartGlintMixin {

    @Unique
    private static final ThreadLocal<AbstractMinecartEntity>
            MINECART_MAGIC$CURRENT_MINECART =
            new ThreadLocal<>();

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void minecartmagic$beginRender(
            AbstractMinecartEntity minecart,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
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
            MatrixStack matrices,
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
    private VertexConsumer minecartmagic$applyTractionGlint(
            VertexConsumerProvider vertexConsumers,
            RenderLayer layer
    ) {
        AbstractMinecartEntity minecart =
                MINECART_MAGIC$CURRENT_MINECART.get();

        if (minecart == null) {
            return vertexConsumers.getBuffer(layer);
        }

        if (ModEnchantments.getTractionLevel(minecart) <= 0) {
            return vertexConsumers.getBuffer(layer);
        }

        return ItemRenderer.getItemGlintConsumer(
                vertexConsumers,
                layer,
                false,
                true
        );
    }
}
