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
         * Пока одна версия:
         * дубовая.
         */
        boat.setVariant(
                BoatEntity.Type.OAK
        );

        /*
         * Направление установки.
         */
        boat.setYaw(
                user.getYaw()
        );

        /*
         * Читаем Tailwind с предмета.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        stack
                );

        if (tailwindLevel > 0) {

            /*
             * Сохраняем стандартную attachment-механику.
             */
            ModEnchantments.setTailwindLevel(
                    boat,
                    tailwindLevel
            );

            /*
             * И дополнительно записываем уровень
             * непосредственно в engine DataTracker.
             *
             * Теперь двигатель гарантированно знает
             * свой уровень Tailwind.
             */
            boat.setEngineTailwindLevel(
                    tailwindLevel
            );
        }

        world.spawnEntity(
                boat
        );

        /*
         * В Survival предмет расходуется.
         *
         * В Creative остаётся.
         */
        if (!user.getAbilities().creativeMode) {

            stack.decrement(1);
        }

        return TypedActionResult.success(
                stack
        );
    }
}
