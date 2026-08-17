package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartEntityRenderer.class)
public class MinecartGlintMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"
            )
    )
    private void minecartmagic$renderTractionGlint(
            Model model,
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
        /*
         * Сначала выполняем обычный ванильный рендер модели.
         */
        model.render(
                matrices,
                vertices,
                light,
                overlay
        );

        /*
         * Тяга хранится на установленной MinecartEntity
         * через command tag.
         */
        if (!(minecart instanceof MinecartEntity normalMinecart)) {
            return;
        }

        int tractionLevel =
                ModEnchantments.getTractionLevel(normalMinecart);

        if (tractionLevel <= 0) {
            return;
        }

        /*
         * Настоящий ванильный ENTITY GLINT.
         *
         * Это именно RenderLayer для 3D-сущностей,
         * а не ItemRenderer item-glint.
         */
        VertexConsumer glint =
                vertexConsumers.getBuffer(
                        RenderLayer.getDirectEntityGlint()
                );

        /*
         * Повторно рисуем ту же самую 3D-модель
         * в том же положении, но через entity glint.
         */
        model.render(
                matrices,
                glint,
                light,
                overlay,
                0xFFFFFFFF
        );
    }
}
