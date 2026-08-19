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
         * Клиент только подтверждает использование.
         * Entity создаём исключительно на сервере.
         */
        if (world.isClient()) {

            return TypedActionResult.success(
                    stack
            );
        }

        /*
         * =====================================================
         * СОЗДАЁМ САМОХОДНУЮ ЛОДКУ
         * =====================================================
         */
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
         * Пока используем дубовую модель.
         */
        boat.setVariant(
                BoatEntity.Type.OAK
        );

        /*
         * Направление появления.
         */
        boat.setYaw(
                user.getYaw()
        );

        /*
         * =====================================================
         * ПОЛУЧАЕМ TAILWIND С ITEMSTACK
         * =====================================================
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

        /*
         * =====================================================
         * СНАЧАЛА SPAWN
         * =====================================================
         *
         * Это важно.
         *
         * Entity теперь полностью зарегистрирована в мире
         * до того, как мы записываем в неё Attachment
         * и DataTracker двигателя.
         */
        world.spawnEntity(
                boat
        );

        /*
         * =====================================================
         * ПЕРЕНОСИМ ЗАЧАРОВАНИЕ ПОСЛЕ SPAWN
         * =====================================================
         *
         * Основная система:
         *
         * Attachment
         *
         * +
         *
         * command tag
         *
         * Резервная система:
         *
         * ENGINE_TAILWIND_LEVEL
         *
         * Оба источника получают один и тот же уровень.
         */
        if (tailwindLevel > 0) {

            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );

            boat.setEngineTailwindLevel(
                    tailwindLevel
            );
        } else {

            /*
             * Явно очищаем tracker для незачарованной лодки.
             */
            boat.setEngineTailwindLevel(
                    0
            );
        }

        /*
         * =====================================================
         * РАСХОД ПРЕДМЕТА
         * =====================================================
         */
        if (!user.getAbilities().creativeMode) {

            stack.decrement(1);
        }

        return TypedActionResult.success(
                stack
        );
    }
}
