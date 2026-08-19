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
         * Работает только с основной рукой.
         */
        if (hand != Hand.MAIN_HAND) {
            return;
        }

        /*
         * Если игрок уже сидит в нашей самоходной лодке,
         * сервер открывает её существующее меню.
         *
         * Сам по себе этот Mixin НЕ вызывается от E.
         * E будет отправлять обычное entity interaction
         * через отдельный клиентский Mixin ниже.
         */
        if (!boat.getPassengerList().contains(player)) {
            return;
        }

        if (!boat.getWorld().isClient()) {
            player.openHandledScreen(boat);
        }

        cir.setReturnValue(
                ActionResult.success(
                        boat.getWorld().isClient()
                )
        );
    }
}
