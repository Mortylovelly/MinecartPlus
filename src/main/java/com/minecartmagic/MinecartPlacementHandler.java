package com.minecartmagic;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
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

        /*
         * Работаем только с обычной вагонеткой.
         */
        if (!stack.isOf(Items.MINECART)) {
            return ActionResult.PASS;
        }

        /*
         * Проверяем настоящее зачарование на предмете.
         */
        int tractionLevel =
                ModEnchantments.getTractionLevel(stack);

        /*
         * Обычная вагонетка должна полностью оставаться
         * ванильной.
         */
        if (tractionLevel <= 0) {
            return ActionResult.PASS;
        }

        BlockPos railPos = hitResult.getBlockPos();

        /*
         * Ставить можно только на рельсы.
         */
        if (!AbstractRailBlock.isRail(world, railPos)) {
            return ActionResult.PASS;
        }

        /*
         * Клиент не создаёт Entity.
         * Это делает только сервер.
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

        /*
         * Проверяем, свободно ли место.
         */
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

        /*
         * Создаём обычную ванильную вагонетку.
         *
         * Никаких SpawnReason.
         * Никаких Mixin.
         */
        MinecartEntity minecart =
                new MinecartEntity(
                        serverWorld,
                        x,
                        y,
                        z
                );

        /*
         * Передаём вагонетке уровень зачарования.
         */
        MinecartTractionHandler.setTractionLevel(
                minecart,
                tractionLevel
        );

        /*
         * Добавляем Entity в мир.
         */
        serverWorld.spawnEntity(minecart);

        /*
         * Забираем одну вагонетку из руки.
         * В Creative предмет не расходуется.
         */
        stack.decrementUnlessCreative(1, player);

        return ActionResult.SUCCESS;
    }
}
