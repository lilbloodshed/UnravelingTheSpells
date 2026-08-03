package org.holy.unraveling_spells.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.config.ClientConfiguration;

import java.util.function.Consumer;

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
        return apiUsable && ModList.get().isLoaded(MOD_ID) && ClientConfiguration.ENABLED_ANIMATIONS.get();
    }

    public static void update(Screen screen) {
        if (!isEnabled()) return;

        try {
            AnimLibAdapter.update(screen);
        } catch (LinkageError | RuntimeException exception) {
            disableApi(exception);
        }
    }

    public static void clear(Screen screen) {
        if (!isEnabled()) return;

        try {
            AnimLibAdapter.clear(screen);
        } catch (LinkageError | RuntimeException exception) {
            disableApi(exception);
        }
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, Component text, int x, int y,
                                int color, boolean shadow, TextEffect... effects) {
        if (isEnabled()) {
            try {
                AnimLibAdapter.drawText(guiGraphics, font, text, x, y, color, shadow, effects);
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
                AnimLibAdapter.drawText(guiGraphics, font, text, x, y, color, shadow, effects);
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
                AnimLibAdapter.blit(guiGraphics, texture, x, y, width, height, effects);
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
                AnimLibAdapter.blit(guiGraphics, texture, x, y, uOffset, vOffset, width, height,
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
                AnimLibAdapter.animate(screen, from, to, duration, easing, setter);
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

    public enum Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT
    }
}
