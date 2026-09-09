package com.minecartmagic.mixin;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.item.MinecartItem.class)
public abstract class MinecartItemPlacementMixin {

    @Inject(method = "useOnBlock", at = @At("RETURN"))
    private void minecartmagic$applyPlacementDirection(
            ItemUsageContext context,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (context.getWorld().isClient() || !cir.getReturnValue().isAccepted()) {
            return;
        }

        World world = context.getWorld();
        BlockPos railPos = context.getBlockPos();
        BlockState state = world.getBlockState(railPos);

        if (!(state.getBlock() instanceof AbstractRailBlock railBlock)) {
            return;
        }

        PlayerLookup:
        {
            if (context.getPlayer() == null) {
                return;
            }

            Vec3d center = Vec3d.ofCenter(railPos).add(0.0D, 0.0625D, 0.0D);
            AbstractMinecartEntity closest = world.getEntitiesByClass(
                    AbstractMinecartEntity.class,
                    new net.minecraft.util.math.Box(
                            center.x - 0.75D, center.y - 0.75D, center.z - 0.75D,
                            center.x + 0.75D, center.y + 0.75D, center.z + 0.75D
                    ),
                    cart -> cart.isAlive()
            ).stream()
                    .min((a, b) -> Double.compare(
                            a.squaredDistanceTo(center),
                            b.squaredDistanceTo(center)
                    ))
                    .orElse(null);

            if (closest == null) {
                return;
            }

            float yaw = getPlacementYaw(
                    state,
                    context.getPlayer().getYaw()
            );

            closest.setYaw(yaw);
            closest.setHeadYaw(yaw);
            closest.setBodyYaw(yaw);
            closest.prevYaw = yaw;

            if (closest instanceof AdvancedMinecartEntity advanced) {
                advanced.setPlacementYaw(yaw);
            }
        }
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
