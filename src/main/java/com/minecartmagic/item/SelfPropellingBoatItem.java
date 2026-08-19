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
         * На клиенте только показываем
         * успешное использование.
         *
         * Создание entity происходит
         * на сервере.
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

        /*
         * Позиция установки.
         */
        boat.setPosition(
                hit.getPos().x,
                hit.getPos().y,
                hit.getPos().z
        );

        /*
         * Пока у нас только одна версия —
         * дубовая.
         */
        boat.setVariant(
                BoatEntity.Type.OAK
        );

        /*
         * Поворачиваем лодку по направлению
         * взгляда игрока.
         */
        boat.setYaw(
                user.getYaw()
        );

        /*
         * =================================================
         * ПОПУТНЫЙ ВЕТЕР
         * =================================================
         *
         * Переносим зачарование с ItemStack
         * на новую entity через уже существующую
         * систему ModEnchantments.
         *
         * Никакого отдельного EngineTailwindLevel
         * здесь больше нет.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

        if (tailwindLevel > 0) {

            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );
        }

        /*
         * Создаём entity в мире.
         */
        world.spawnEntity(
                boat
        );

        /*
         * Creative:
         * предмет не расходуется.
         *
         * Survival:
         * одна самоходная лодка исчезает
         * из руки.
         */
        if (!user.getAbilities().creativeMode) {

            stack.decrement(1);
        }

        return TypedActionResult.success(
                stack
        );
    }
}
