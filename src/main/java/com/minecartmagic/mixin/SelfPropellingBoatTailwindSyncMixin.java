package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelfPropellingBoatEntity.class)
public abstract class SelfPropellingBoatTailwindSyncMixin {

    /*
     * SelfPropellingBoatEntity раньше каждый серверный тик
     * выполнял:
     *
     * ENGINE_TAILWIND_LEVEL =
     *     ModEnchantments.getTailwindLevel(this);
     *
     * Если Attachment в этот момент возвращал 0,
     * установленный уровень I/II/III стирался.
     *
     * Для самоходной лодки этого больше делать нельзя.
     *
     * Уровень двигателя уже записан непосредственно
     * при создании сущности из зачарованного ItemStack.
     */
    @Inject(
            method = "synchronizeTailwindLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$keepEngineTailwindLevel(
            CallbackInfo ci
    ) {

        /*
         * Полностью отменяем старую синхронизацию,
         * которая могла заменить I/II/III на 0.
         */
        ci.cancel();
    }
}
