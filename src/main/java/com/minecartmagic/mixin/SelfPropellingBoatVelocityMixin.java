package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatVelocityMixin {

    @Shadow
    private boolean pressingLeft;

    @Shadow
    private boolean pressingRight;

    @Inject(
            method = "updateVelocity",
            at = @At("TAIL")
    )
    private void minecartmagic$applyEngineAfterVanillaPhysics(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Без топлива:
         * полностью ванильная физика лодки.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * На суше двигатель не вмешивается.
         *
         * Ванильная физика уже отработала:
         * гравитация остаётся нормальной.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        /*
         * ДВИЖЕНИЕ И РУЛЕНИЕ ДЕЛАЕМ ТОЛЬКО
         * НА СЕРВЕРНОЙ КОПИИ.
         *
         * Это важно:
         * сервер является источником истины
         * для velocity и уровня Tailwind.
         */
        if (selfPropellingBoat.getWorld().isClient()) {
            return;
        }

        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                false
        );
    }
}
