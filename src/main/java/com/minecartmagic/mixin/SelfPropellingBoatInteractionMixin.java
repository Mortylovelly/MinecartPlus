package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SelfPropellingBoatEntity.class)
public abstract class SelfPropellingBoatInteractionMixin {

    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$openMenuWhileRiding(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        SelfPropellingBoatEntity boat =
                (SelfPropellingBoatEntity) (Object) this;

        /*
         * Только основная рука.
         */
        if (hand != Hand.MAIN_HAND) {
            return;
        }

        /*
         * Если игрок уже является пассажиром этой лодки,
         * interaction используется для открытия её GUI.
         *
         * ПКМ / Shift здесь НЕ проверяем.
         *
         * Клиентский MinecartMagicClient ниже отправляет
         * сюда interaction именно по клавише Inventory (E).
         */
        if (!boat.getPassengerList().contains(player)) {
            return;
        }

        if (!boat.getWorld().isClient()) {
            player.openHandledScreen(
                    boat
            );
        }

        cir.setReturnValue(
                ActionResult.success(
                        boat.getWorld().isClient()
                )
        );
    }
}
