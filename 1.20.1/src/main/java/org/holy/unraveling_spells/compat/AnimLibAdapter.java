package org.holy.unraveling_spells.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.holy.animlib.api.AnimLib;
import org.holy.animlib.api.text.TextEffect;
import org.holy.animlib.api.text.TextEffects;
import org.holy.animlib.runtime.animation.animation.Easing;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The only class that links directly against the optional AnimLib API.
 * It is loaded only after {@link AnimationCompat} verifies that AnimLib is present.
 */
final class AnimLibAdapter {
    private AnimLibAdapter() {
    }

    static void update(Screen screen) {
        AnimLib.update(screen);
    }

    static void clear(Screen screen) {
        AnimLib.clear(screen);
    }

    static void drawText(GuiGraphics graphics, Font font, Component text, int x, int y,
                         int color, boolean shadow, AnimationCompat.TextEffect[] effects) {
        AnimLib.drawText(graphics, font, text, x, y, color, shadow, convertEffects(effects));
    }

    static void drawText(GuiGraphics graphics, Font font, String text, int x, int y,
                         int color, boolean shadow, AnimationCompat.TextEffect[] effects) {
        AnimLib.drawText(graphics, font, text, x, y, color, shadow, convertEffects(effects));
    }

    static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                     int width, int height, AnimationCompat.TextEffect[] effects) {
        AnimLib.blit(graphics, texture, x, y, width, height, convertEffects(effects));
    }

    static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                     int uOffset, int vOffset, int width, int height,
                     int textureWidth, int textureHeight, AnimationCompat.TextEffect[] effects) {
        AnimLib.blit(graphics, texture, x, y, uOffset, vOffset, width, height,
                textureWidth, textureHeight, convertEffects(effects));
    }

    static void animate(Screen screen, float from, float to, float duration,
                        AnimationCompat.Easing easing, Consumer<Float> setter) {
        AnimLib.animate(screen)
                .from(from)
                .to(to)
                .duration(duration)
                .easing(easingFunction(easing))
                .bind(setter)
                .start();
    }

    private static Function<Float, Float> easingFunction(AnimationCompat.Easing easing) {
        return switch (easing) {
            case LINEAR -> Easing::linear;
            case EASE_IN -> Easing::easeIn;
            case EASE_OUT -> Easing::easeOut;
            case EASE_IN_OUT -> Easing::easeInOut;
        };
    }

    private static TextEffect[] convertEffects(AnimationCompat.TextEffect[] effects) {
        if (effects == null || effects.length == 0) {
            return new TextEffect[0];
        }

        return Arrays.stream(effects)
                .filter(effect -> effect != null)
                .map(AnimLibAdapter::convertEffect)
                .toArray(TextEffect[]::new);
    }

    private static TextEffect convertEffect(AnimationCompat.TextEffect effect) {
        return switch (effect.type()) {
            case SHAKE -> TextEffects.shake(effect.amount(), effect.speed());
            case TURBULENCE -> TextEffects.turbulence(effect.amount(), effect.speed());
        };
    }
}
