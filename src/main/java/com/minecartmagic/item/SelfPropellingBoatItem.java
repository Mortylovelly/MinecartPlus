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
         * Создание entity производится на сервере.
         */
        if (world.isClient()) {

            return TypedActionResult.success(
                    stack
            );
        }

        /*
         * =====================================================
         * ПОЛУЧАЕМ УРОВЕНЬ TAILWIND С ПРЕДМЕТА
         * =====================================================
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

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

        boat.setVariant(
                BoatEntity.Type.OAK
        );

        boat.setYaw(
                user.getYaw()
        );

        /*
         * =====================================================
         * СНАЧАЛА РЕГИСТРИРУЕМ ENTITY В МИРЕ
         * =====================================================
         *
         * Это принципиально важно для attachments,
         * command tags и DataTracker.
         */
        world.spawnEntity(
                boat
        );

        /*
         * =====================================================
         * ПОСЛЕ SPAWN ПЕРЕНОСИМ TAILWIND
         * =====================================================
         */
        if (tailwindLevel > 0) {

            /*
             * Основное хранилище мода.
             */
            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );

            /*
             * Отдельное синхронизированное значение
             * двигателя.
             */
            boat.setEngineTailwindLevel(
                    tailwindLevel
            );

            /*
             * Дополнительная явная запись command tag.
             *
             * ModEnchantments.setTailwindLevel()
             * уже делает это, но здесь оставляем
             * дополнительную гарантию.
             */
            boat.addCommandTag(
                    ModEnchantments.getTailwindTag(
                            tailwindLevel
                    )
            );

        } else {

            /*
             * Незачарованная самоходная лодка.
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
