package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntityRenderer.class)
public class BoatTailwindGlintMixin {

    @Unique
    private static final ThreadLocal<BoatEntity>
            MINECART_MAGIC$CURRENT_BOAT =
            new ThreadLocal<>();

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void minecartmagic$beginRender(
            BoatEntity boat,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MINECART_MAGIC$CURRENT_BOAT.set(boat);
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void minecartmagic$endRender(
            BoatEntity boat,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MINECART_MAGIC$CURRENT_BOAT.remove();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private VertexConsumer minecartmagic$applyTailwindGlint(
            VertexConsumerProvider vertexConsumers,
            RenderLayer layer
    ) {
        BoatEntity boat =
                MINECART_MAGIC$CURRENT_BOAT.get();

        if (boat == null) {
            return vertexConsumers.getBuffer(layer);
        }

        if (ModEnchantments.getTailwindLevel(boat) <= 0) {
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
