package com.minecartmagic.mixin;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.item.MinecartItem.class)
public abstract class MinecartItemPlacementMixin {

    @Unique
    private float minecartmagic$placementYaw;

    @Unique
    private boolean minecartmagic$placementDirectionAvailable;

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void minecartmagic$capturePlacementDirection(
            ItemUsageContext context,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        minecartmagic$placementDirectionAvailable = false;

        if (context.getPlayer() == null) {
            return;
        }

        BlockState state = context.getWorld().getBlockState(
                context.getBlockPos()
        );

        if (!(state.getBlock() instanceof AbstractRailBlock)) {
            return;
        }

        minecartmagic$placementYaw = getPlacementYaw(
                state,
                context.getPlayer().getYaw()
        );
        minecartmagic$placementDirectionAvailable = true;
    }

    @ModifyArg(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
            ),
            index = 0
    )
    private Entity minecartmagic$applyPlacementDirection(Entity entity) {
        if (minecartmagic$placementDirectionAvailable
                && entity instanceof FurnaceMinecartEntity minecart) {
            minecart.setYaw(minecartmagic$placementYaw);
            minecart.setHeadYaw(minecartmagic$placementYaw);
            minecart.setBodyYaw(minecartmagic$placementYaw);
            minecart.prevYaw = minecartmagic$placementYaw;
        }

        return entity;
    }

    @Unique
    private static float getPlacementYaw(BlockState state, float playerYaw) {
        RailShape shape = state.get(
                ((AbstractRailBlock) state.getBlock()).getShapeProperty()
        );

        double radians = Math.toRadians(playerYaw);
        Vec3d look = new Vec3d(
                -Math.sin(radians),
                0.0D,
                Math.cos(radians)
        );

        Vec3d railDirection = switch (shape) {
            case EAST_WEST,
                    ASCENDING_EAST,
                    ASCENDING_WEST -> new Vec3d(1.0D, 0.0D, 0.0D);
            case NORTH_SOUTH,
                    ASCENDING_NORTH,
                    ASCENDING_SOUTH -> new Vec3d(0.0D, 0.0D, 1.0D);
            case SOUTH_EAST -> new Vec3d(1.0D, 0.0D, 1.0D).normalize();
            case SOUTH_WEST -> new Vec3d(-1.0D, 0.0D, 1.0D).normalize();
            case NORTH_EAST -> new Vec3d(1.0D, 0.0D, -1.0D).normalize();
            case NORTH_WEST -> new Vec3d(-1.0D, 0.0D, -1.0D).normalize();
        };

        if (look.dotProduct(railDirection) < 0.0D) {
            railDirection = railDirection.multiply(-1.0D);
        }

        return MathHelper.wrapDegrees((float) Math.toDegrees(
                Math.atan2(-railDirection.x, railDirection.z)
        ));
    }
}
