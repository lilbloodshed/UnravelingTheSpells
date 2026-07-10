package org.holy.unraveling_spells.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.config.Configuration;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Optional integration boundary for AnimLib.
 *
 * <Classes outside this file must not reference AnimLib types. Keeping the direct
 * API calls in the lazily loaded bridge allows the rest of the client to work when
 * AnimLib is not installed
 */
public final class AnimationCompat {
    private static final String MOD_ID = "animlib";
    private static boolean apiUsable = true;
    private static boolean missingApiLogged;

    private AnimationCompat() {
    }

    public static boolean isEnabled() {
        return apiUsable && ModList.get().isLoaded(MOD_ID) && Configuration.ENABLED_ANIMATIONS.get();
    }

    public static void update(Screen screen) {
        if (!isEnabled()) return;

        try {
            AnimLibBridge.update(screen);
        } catch (LinkageError | RuntimeException exception) {
            disableApi(exception);
        }
    }

    public static void clear(Screen screen) {
        if (!isEnabled()) return;

        try {
            AnimLibBridge.clear(screen);
        } catch (LinkageError | RuntimeException exception) {
            disableApi(exception);
        }
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, Component text, int x, int y,
                                int color, boolean shadow, TextEffect... effects) {
        if (isEnabled()) {
            try {
                AnimLibBridge.drawText(guiGraphics, font, text, x, y, color, shadow, effects);
                return;
            } catch (LinkageError | RuntimeException exception) {
                disableApi(exception);
            }
        }

        guiGraphics.drawString(font, text, x, y, color, shadow);
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y,
                                int color, boolean shadow, TextEffect... effects) {
        if (isEnabled()) {
            try {
                AnimLibBridge.drawText(guiGraphics, font, text, x, y, color, shadow, effects);
                return;
            } catch (LinkageError | RuntimeException exception) {
                disableApi(exception);
            }
        }

        guiGraphics.drawString(font, text, x, y, color, shadow);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                            int width, int height, TextEffect... effects) {
        if (isEnabled()) {
            try {
                AnimLibBridge.blit(guiGraphics, texture, x, y, width, height, effects);
                return;
            } catch (LinkageError | RuntimeException exception) {
                disableApi(exception);
            }
        }

        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                            int uOffset, int vOffset, int width, int height,
                            int textureWidth, int textureHeight, TextEffect... effects) {
        if (isEnabled()) {
            try {
                AnimLibBridge.blit(guiGraphics, texture, x, y, uOffset, vOffset, width, height,
                        textureWidth, textureHeight, effects);
                return;
            } catch (LinkageError | RuntimeException exception) {
                disableApi(exception);
            }
        }

        guiGraphics.blit(texture, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    public static void animate(Screen screen, float from, float to, float duration,
                               Easing easing, Consumer<Float> setter) {
        if (isEnabled()) {
            try {
                AnimLibBridge.animate(screen, from, to, duration, easing, setter);
                return;
            } catch (LinkageError | RuntimeException exception) {
                disableApi(exception);
            }
        }

        setter.accept(to);
    }

    private static void disableApi(Throwable throwable) {
        apiUsable = false;
        if (!missingApiLogged) {
            missingApiLogged = true;
            Unraveling_spells.LOGGER.warn(
                    "AnimLib is installed and GUI animations are enabled, but its API could not be used. " +
                            "Animations will fall back to their final state.", throwable);
        }
    }

    public enum Easing {
        LINEAR,
        EASE_IN_OUT,
        EASE_OUT,
        EASE_IN
    }

    public record TextEffect(Type type, float amount, float speed) {
        public static TextEffect shake(float amount, float speed) {
            return new TextEffect(Type.SHAKE, amount, speed);
        }

        public static TextEffect turbulence(float amount, float speed) {
            return new TextEffect(Type.TURBULENCE, amount, speed);
        }

        public enum Type {
            SHAKE,
            TURBULENCE
        }
    }

    /** Loaded only after Forge confirms that AnimLib is installed. */
    private static final class AnimLibBridge {
        private AnimLibBridge() {
        }

        private static void update(Screen screen) {
            org.holy.animlib.api.AnimLib.update(screen);
        }

        private static void clear(Screen screen) {
            org.holy.animlib.api.AnimLib.clear(screen);
        }

        private static void drawText(GuiGraphics guiGraphics, Font font, Component text, int x, int y,
                                     int color, boolean shadow, TextEffect[] effects) {
            org.holy.animlib.api.AnimLib.drawText(guiGraphics, font, text, x, y, color, shadow,
                    convertEffects(effects));
        }

        private static void drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y,
                                     int color, boolean shadow, TextEffect[] effects) {
            org.holy.animlib.api.AnimLib.drawText(guiGraphics, font, text, x, y, color, shadow,
                    convertEffects(effects));
        }

        private static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                                 int width, int height, TextEffect[] effects) {
            org.holy.animlib.api.AnimLib.blit(guiGraphics, texture, x, y, width, height,
                    convertEffects(effects));
        }

        private static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                                 int uOffset, int vOffset, int width, int height,
                                 int textureWidth, int textureHeight, TextEffect[] effects) {
            org.holy.animlib.api.AnimLib.blit(guiGraphics, texture, x, y, uOffset, vOffset,
                    width, height, textureWidth, textureHeight, convertEffects(effects));
        }

        private static void animate(Screen screen, float from, float to, float duration,
                                    Easing easing, Consumer<Float> setter) {
            org.holy.animlib.api.AnimLib.animate(screen)
                    .from(from)
                    .to(to)
                    .duration(duration)
                    .easing(easingFunction(easing))
                    .bind(setter)
                    .start();
        }

        private static Function<Float, Float> easingFunction(Easing easing) {
            return switch (easing) {
                case LINEAR -> org.holy.animlib.runtime.animation.animation.Easing::linear;
                case EASE_IN_OUT -> org.holy.animlib.runtime.animation.animation.Easing::easeInOut;
                case EASE_OUT -> org.holy.animlib.runtime.animation.animation.Easing::easeOut;
                case EASE_IN -> org.holy.animlib.runtime.animation.animation.Easing::easeIn;
            };
        }

        private static org.holy.animlib.api.text.TextEffect[] convertEffects(TextEffect[] effects) {
            if (effects == null || effects.length == 0) {
                return new org.holy.animlib.api.text.TextEffect[0];
            }

            return Arrays.stream(effects)
                    .filter(effect -> effect != null)
                    .map(AnimLibBridge::convertEffect)
                    .toArray(org.holy.animlib.api.text.TextEffect[]::new);
        }

        private static org.holy.animlib.api.text.TextEffect convertEffect(TextEffect effect) {
            return switch (effect.type()) {
                case SHAKE -> org.holy.animlib.api.text.TextEffects.shake(effect.amount(), effect.speed());
                case TURBULENCE -> org.holy.animlib.api.text.TextEffects.turbulence(effect.amount(), effect.speed());
            };
        }
    }
}
