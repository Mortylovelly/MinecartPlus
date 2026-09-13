package com.minecartmagic;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class MinecartPlacementHandler {

    private MinecartPlacementHandler() {
    }

    public static void init() {
        UseBlockCallback.EVENT.register(
                MinecartPlacementHandler::onUseBlock
        );
    }

    private static ActionResult onUseBlock(
            PlayerEntity player,
            World world,
            Hand hand,
            BlockHitResult hitResult
    ) {
        ItemStack stack = player.getStackInHand(hand);

        AbstractMinecartEntity.Type minecartType =
                getMinecartType(stack);

        if (minecartType == null) {
            return ActionResult.PASS;
        }

        int tractionLevel =
                ModEnchantments.getTractionLevel(stack);

        if (tractionLevel <= 0) {
            return ActionResult.PASS;
        }

        BlockPos railPos = hitResult.getBlockPos();

        if (!AbstractRailBlock.isRail(world, railPos)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        if (!player.canModifyAt(serverWorld, railPos)) {
            return ActionResult.FAIL;
        }

        double x = railPos.getX() + 0.5D;
        double y = railPos.getY() + 0.0625D;
        double z = railPos.getZ() + 0.5D;

        Box collisionBox = new Box(
                x - 0.49D,
                y,
                z - 0.49D,
                x + 0.49D,
                y + 0.7D,
                z + 0.49D
        );

        if (!serverWorld.isSpaceEmpty(collisionBox)) {
            return ActionResult.FAIL;
        }

        AbstractMinecartEntity minecart =
                AbstractMinecartEntity.create(
                        serverWorld,
                        x,
                        y,
                        z,
                        minecartType
                );

        if (minecart == null) {
            return ActionResult.FAIL;
        }

        BlockState railState = serverWorld.getBlockState(railPos);
        float placementYaw = getPlacementYaw(
                railState,
                player.getYaw()
        );

        minecart.setYaw(placementYaw);
        minecart.setHeadYaw(placementYaw);
        minecart.setBodyYaw(placementYaw);
        minecart.prevYaw = placementYaw;

        ModEnchantments.setTractionLevel(
                minecart,
                tractionLevel
        );

        serverWorld.spawnEntity(minecart);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    private static float getPlacementYaw(
            BlockState state,
            float playerYaw
    ) {
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

    private static AbstractMinecartEntity.Type getMinecartType(
            ItemStack stack
    ) {
        if (stack.isOf(Items.MINECART)) {
            return AbstractMinecartEntity.Type.RIDEABLE;
        }

        if (stack.isOf(Items.CHEST_MINECART)) {
            return AbstractMinecartEntity.Type.CHEST;
        }

        if (stack.isOf(Items.FURNACE_MINECART)) {
            return AbstractMinecartEntity.Type.FURNACE;
        }

        if (stack.isOf(Items.HOPPER_MINECART)) {
            return AbstractMinecartEntity.Type.HOPPER;
        }

        if (stack.isOf(Items.TNT_MINECART)) {
            return AbstractMinecartEntity.Type.TNT;
        }

        if (stack.isOf(Items.COMMAND_BLOCK_MINECART)) {
            return AbstractMinecartEntity.Type.COMMAND_BLOCK;
        }

        return null;
    }

    public static String getTractionTag(int level) {
        return switch (level) {
            case 1 -> "minecartmagic_traction_1";
            case 2 -> "minecartmagic_traction_2";
            case 3 -> "minecartmagic_traction_3";
            default -> "minecartmagic_traction_0";
        };
    }
}
