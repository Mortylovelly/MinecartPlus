package com.minecartmagic;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
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
            net.minecraft.entity.player.PlayerEntity player,
            World world,
            Hand hand,
            BlockHitResult hitResult
    ) {
        ItemStack stack = player.getStackInHand(hand);

        // Только обычная вагонетка.
        if (!stack.isOf(Items.MINECART)) {
            return ActionResult.PASS;
        }

        int tractionLevel = ModEnchantments.getTractionLevel(stack);

        // Незачарованная вагонетка должна работать полностью ванильно.
        if (tractionLevel <= 0) {
            return ActionResult.PASS;
        }

        BlockPos railPos = hitResult.getBlockPos();

        // Ставим только на настоящий рельс.
        if (!AbstractRailBlock.isRail(world, railPos)) {
            return ActionResult.PASS;
        }

        /*
         * На клиенте просто отменяем ванильную обработку.
         * Настоящее создание сущности выполняется сервером.
         */
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

        AbstractMinecartEntity minecart = AbstractMinecartEntity.create(
                serverWorld,
                x,
                y,
                z,
                EntityType.MINECART,
                SpawnReason.SPAWN_ITEM_USE,
                stack,
                player
        );

        if (minecart == null) {
            return ActionResult.FAIL;
        }

        /*
         * Сохраняем уровень Тяги непосредственно на entity.
         * Поэтому после установки нам больше НЕ нужно пытаться
         * получать зачарование через getPickBlockStack().
         */
        MinecartTractionHandler.setTractionLevel(
                minecart,
                tractionLevel
        );

        // Видимый индикатор зачарованной вагонетки.
        minecart.setGlowing(true);

        serverWorld.spawnEntity(minecart);

        // Забираем один предмет только в выживании.
        stack.decrementUnlessCreative(1, player);

        return ActionResult.SUCCESS;
    }
}
