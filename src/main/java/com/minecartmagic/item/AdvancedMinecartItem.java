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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

        PlayerEntity player = context.getPlayer();
        float playerYaw = player == null ? 0.0F : player.getYaw();
        float yaw = getPlacementYaw(state, playerYaw);

        minecart.setYaw(yaw);
        minecart.setHeadYaw(yaw);
        minecart.setBodyYaw(yaw);
        minecart.prevYaw = yaw;
        minecart.setPlacementYaw(yaw);

        world.spawnEntity(minecart);

        if (player == null || !player.isCreative()) {
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }

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
