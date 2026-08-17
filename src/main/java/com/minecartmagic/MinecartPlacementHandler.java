package com.minecartmagic;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractRailBlock;
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
                        minecartType,
                        stack,
                        player
                );

        if (minecart == null) {
            return ActionResult.FAIL;
        }

        ModEnchantments.setTractionLevel(
                minecart,
                tractionLevel
        );

        serverWorld.spawnEntity(minecart);

        stack.decrementUnlessCreative(1, player);

        return ActionResult.SUCCESS;
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
