package com.minecartmagic.item;

import com.minecartmagic.ModEntities;
import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
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

        PlayerEntity player = context.getPlayer();
        AdvancedMinecartEntity minecart =
                ModEntities.ADVANCED_MINECART.create(world);

        if (minecart == null) {
            return ActionResult.FAIL;
        }

        minecart.setPosition(
                pos.getX() + 0.5D,
                pos.getY() + 0.0625D,
                pos.getZ() + 0.5D
        );
        minecart.setYaw(player != null ? player.getYaw() : 0.0F);
        world.spawnEntity(minecart);

        if (player == null || !player.isCreative()) {
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}
