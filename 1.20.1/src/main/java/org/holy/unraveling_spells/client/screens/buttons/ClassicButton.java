package org.holy.unraveling_spells.client.screens.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;
import org.holy.unraveling_spells.compat.AnimationCompat;

public class ClassicButton extends Button {
    private Component title;

    public ClassicButton(int x, int y, int w, int h, Component title) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(w, h));
        this.title = title;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        if (isActive()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                        getWidth(), getHeight(),
                        4, 4,
                        4, 4,
                        32, 16,
                        64, 0);
            }
            else {
                guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                        getWidth(), getHeight(),
                        4, 4,
                        4, 4,
                        32, 16,
                        64, 32);
            }
        } else {
            guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                    getWidth(), getHeight(),
                    4, 4,
                    4, 4,
                    32, 16,
                    64, 16);
        }

        float fillProgress = Math.max(0.0f, Math.min(1.0f, getFillProgress()));
        if (fillProgress > 0.0f) {
            int fillWidth = Math.round((getWidth() - 2) * fillProgress);
            int fillAlpha = Math.round(96.0f * this.alpha);
            guiGraphics.fill(getX() + 1, getY() + 1,
                    getX() + 1 + fillWidth, getY() + getHeight() - 1,
                    (fillAlpha << 24) | 0xFFFFFF);
        }

        String buttonTitle = getTitle();
        int titleX = getX() + (getWidth() - Minecraft.getInstance().font.width(buttonTitle)) / 2;
        int titleY = getY() + (getHeight() / 2) - 3;
        if (isActive()) {
            int color = ((int) (this.alpha * 255.0f) << 24) | MagicLecternScreen.FONT_COLOR;
            AnimationCompat.drawText(guiGraphics, Minecraft.getInstance().font, buttonTitle,
                    titleX, titleY, color, true, getTitleEffect());
        } else {
            int color = ((int) (this.alpha * 255.0f) << 24) | MagicLecternScreen.FONTDISABLED_COLOR;
            AnimationCompat.drawText(guiGraphics, Minecraft.getInstance().font, buttonTitle,
                    titleX, titleY, color, true, getTitleEffect());
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected float getFillProgress() {
        return 0.0f;
    }

    protected AnimationCompat.TextEffect getTitleEffect() {
        return null;
    }

    public String getTitle() {
        return title.getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isActive() && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return isActive() && super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (isActive()) {
            super.playDownSound(soundManager);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isActive() && mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
