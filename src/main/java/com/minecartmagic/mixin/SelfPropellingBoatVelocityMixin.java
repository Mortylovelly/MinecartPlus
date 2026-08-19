package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatVelocityMixin {

    private static final String TAILWIND_TAG_PREFIX =
            "minecartmagic_tailwind_";

    @Inject(
            method = "updateVelocity",
            at = @At("TAIL")
    )
    private void minecartmagic$applySelfPropulsion(
            CallbackInfo ci
    ) {

        BoatEntity boat =
                (BoatEntity) (Object) this;

        /*
         * Работаем только с нашей сущностью.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {

            return;
        }

        /*
         * Без топлива двигатель не работает.
         */
        if (!selfPropellingBoat.hasFuel()) {

            return;
        }

        /*
         * На суше двигатель не работает.
         */
        if (!selfPropellingBoat.isTouchingWater()) {

            return;
        }

        /*
         * =====================================================
         * ИЩЕМ TAILWIND НЕПОСРЕДСТВЕННО НА ENTITY
         * =====================================================
         *
         * Источник №1:
         * Attachment
         *
         * Источник №2:
         * command tag
         *
         * Источник №3:
         * engine tracker.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * =====================================================
         * COMMAND TAG — ЖЁСТКИЙ FALLBACK
         * =====================================================
         */
        int tagLevel = 0;

        for (String tag :
                selfPropellingBoat.getCommandTags()) {

            if (!tag.startsWith(
                    TAILWIND_TAG_PREFIX
            )) {

                continue;
            }

            try {

                int parsedLevel =
                        Integer.parseInt(
                                tag.substring(
                                        TAILWIND_TAG_PREFIX.length()
                                )
                        );

                if (parsedLevel > tagLevel) {

                    tagLevel =
                            parsedLevel;
                }

            } catch (NumberFormatException ignored) {

                /*
                 * Просто игнорируем битый tag.
                 */
            }
        }

        /*
         * Если command tag содержит уровень,
         * доверяем ему.
         */
        if (tagLevel > tailwindLevel) {

            tailwindLevel =
                    tagLevel;
        }

        /*
         * =====================================================
         * ENGINE TRACKER — ПОСЛЕДНИЙ FALLBACK
         * =====================================================
         */
        int trackedLevel =
                selfPropellingBoat.getEngineTailwindLevel();

        if (tailwindLevel <= 0
                && trackedLevel > 0) {

            tailwindLevel =
                    trackedLevel;
        }

        /*
         * Защита диапазона I-III.
         */
        tailwindLevel =
                Math.max(
                        0,
                        Math.min(
                                3,
                                tailwindLevel
                        )
                );

        /*
         * =====================================================
         * СИНХРОНИЗИРУЕМ ENGINE TRACKER
         * =====================================================
         */
        if (tailwindLevel > 0
                && tailwindLevel
                        != trackedLevel) {

            selfPropellingBoat.setEngineTailwindLevel(
                    tailwindLevel
            );
        }

        /*
         * =====================================================
         * ПРИМЕНЯЕМ ДВИГАТЕЛЬ
         * =====================================================
         *
         * applySelfPropulsion()
         * берёт getMaximumSpeed(),
         * а он теперь получает тот же уровень,
         * который мы только что гарантированно
         * установили в ENGINE_TAILWIND_LEVEL.
         */
        selfPropellingBoat.applySelfPropulsion();
    }
}
