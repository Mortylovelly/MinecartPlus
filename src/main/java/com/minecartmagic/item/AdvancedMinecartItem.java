package com.minecartmagic.item;

import com.minecartmagic.ModEntities;
import com.minecartmagic.ModItems;
import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AdvancedMinecartItem extends Item {

    public AdvancedMinecartItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!state.isIn(net.minecraft.registry.tag.BlockTags.RAILS)) {
            return ActionResult.FAIL;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        AdvancedMinecartEntity minecart = ModEntities.ADVANCED_MINECART.create(world);
        if (minecart == null) {
            return ActionResult.FAIL;
        }

        minecart.setPosition(
                pos.getX() + 0.5D,
                pos.getY() + 0.0625D,
                pos.getZ() + 0.5D
        );

        float yaw = getRailYaw(state);
        minecart.setYaw(yaw);
        minecart.setBodyYaw(yaw);
        minecart.prevYaw = yaw;

        world.spawnEntity(minecart);

        PlayerEntity player = context.getPlayer();
        if (player == null || !player.isCreative()) {
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    private static float getRailYaw(BlockState state) {
        RailShape shape = state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty());

        return switch (shape) {
            case EAST_WEST,
                    ASCENDING_EAST,
                    ASCENDING_WEST -> 90.0F;
            case NORTH_SOUTH,
                    ASCENDING_NORTH,
                    ASCENDING_SOUTH -> 0.0F;
            case SOUTH_EAST,
                    SOUTH_WEST,
                    NORTH_EAST,
                    NORTH_WEST -> 45.0F;
        };
    }
}
