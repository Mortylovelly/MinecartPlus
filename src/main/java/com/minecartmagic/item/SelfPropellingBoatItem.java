package com.minecartmagic.item;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModEntities;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class SelfPropellingBoatItem extends Item {

    private final BoatEntity.Type variant;

    public SelfPropellingBoatItem(
            Item.Settings settings
    ) {
        this(
                settings,
                BoatEntity.Type.OAK
        );
    }

    public SelfPropellingBoatItem(
            Item.Settings settings,
            BoatEntity.Type variant
    ) {
        super(settings);
        this.variant = variant;
    }

    public BoatEntity.Type getVariant() {
        return variant;
    }

    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity user,
            Hand hand
    ) {

        ItemStack stack =
                user.getStackInHand(hand);

        BlockHitResult hit =
                Item.raycast(
                        world,
                        user,
                        RaycastContext.FluidHandling.ANY
                );

        if (hit.getType()
                == HitResult.Type.MISS) {

            return TypedActionResult.pass(
                    stack
            );
        }

        /*
         * Клиент только подтверждает использование.
         * Entity создаётся на сервере.
         */
        if (world.isClient()) {

            return TypedActionResult.success(
                    stack
            );
        }

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

        SelfPropellingBoatEntity boat =
                new SelfPropellingBoatEntity(
                        ModEntities.SELF_PROPELLING_BOAT,
                        world
                );

        boat.setPosition(
                hit.getPos().x,
                hit.getPos().y,
                hit.getPos().z
        );

        boat.setVariant(
                variant
        );

        boat.setYaw(
                user.getYaw()
        );

        world.spawnEntity(
                boat
        );

        if (tailwindLevel > 0) {

            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );

            boat.setEngineTailwindLevel(
                    tailwindLevel
            );

            boat.addCommandTag(
                    ModEnchantments.getTailwindTag(
                            tailwindLevel
                    )
            );

        } else {

            boat.setEngineTailwindLevel(
                    0
            );
        }

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.success(
                stack
        );
    }
}
