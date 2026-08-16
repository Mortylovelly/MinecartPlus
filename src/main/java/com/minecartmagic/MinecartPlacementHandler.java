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
         * Получаем настоящее зачарование с предмета.
         */
        int tractionLevel =
                ModEnchantments.getTractionLevel(stack);

        /*
         * Обычная вагонетка полностью остаётся ванильной.
         */
        if (tractionLevel <= 0) {
            return ActionResult.PASS;
        }

        BlockPos railPos = hitResult.getBlockPos();

        /*
         * Ставить зачарованную вагонетку можно только на рельсы.
         */
        if (!AbstractRailBlock.isRail(world, railPos)) {
            return ActionResult.PASS;
        }

        /*
         * Entity создаём только на сервере.
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
         * Проверяем свободно ли место.
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
         */
        MinecartEntity minecart =
                new MinecartEntity(
                        serverWorld,
                        x,
                        y,
                        z
                );

        /*
         * Записываем уровень Тяги в command tag Entity.
         *
         * 1 = Тяга I
         * 2 = Тяга II
         * 3 = Тяга III
         */
        minecart.addCommandTag(
                getTractionTag(tractionLevel)
        );

        /*
         * Добавляем Entity в мир.
         */
        serverWorld.spawnEntity(minecart);

        /*
         * Забираем один предмет в Survival.
         * В Creative предмет не расходуется.
         */
        stack.decrementUnlessCreative(1, player);

        return ActionResult.SUCCESS;
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
