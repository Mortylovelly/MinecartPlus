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

    public SelfPropellingBoatItem(
            Item.Settings settings
    ) {
        super(settings);
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
         * Client:
         * только подтверждаем использование.
         */
        if (world.isClient()) {

            return TypedActionResult.success(
                    stack
            );
        }

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

        /*
         * Пока только дубовый вариант.
         */
        boat.setVariant(
                BoatEntity.Type.OAK
        );

        boat.setYaw(
                user.getYaw()
        );

        /*
         * =================================================
         * TAILWIND
         * =================================================
         */

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

        if (tailwindLevel > 0) {

            /*
             * Основная система attachment.
             */
            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );

            /*
             * Отдельно записываем уровень,
             * который используется двигателем.
             */
            boat.setEngineTailwindLevel(
                    tailwindLevel
            );
        }

        /*
         * Создаём лодку.
         */
        world.spawnEntity(
                boat
        );

        /*
         * Creative:
         * предмет остаётся.
         *
         * Survival:
         * лодка расходуется.
         */
        if (!user.getAbilities().creativeMode) {

            stack.decrement(1);
        }

        return TypedActionResult.success(
                stack
        );
    }
}
