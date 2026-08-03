package org.holy.unraveling_spells.client.screens.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;

public class SpecialButton extends Button {
    public SpecialButton(int x, int y, int w, int h) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(w, h));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);

        if (!isSchoolContains()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                        getWidth(), getHeight(),
                        10, 10,
                        10, 10,
                        56, 32,
                        0, 48);
            }
            else {
                guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                        getWidth(), getHeight(),
                        10, 10,
                        10, 10,
                        56, 32,
                        0, 80);
            }
        } else {
            guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                    getWidth(), getHeight(),
                    10, 10,
                    10, 10,
                    56, 32,
                    56, 80);
        }
    }

    public boolean isSchoolContains() {
        return false;
    }

    public int getTitleColor() {
        return ((int) (this.alpha * 255.0f) << 24) | 0xD9CAD5;
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
