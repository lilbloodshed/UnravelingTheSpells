package org.holy.unraveling_spells.client.screens.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;

public class MainButton extends Button {
    private Component title;

    public MainButton(int x, int y, int w, int h, Component title) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(w, h));
        this.title = title;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int p_93658_, int p_93659_, float p_93660_) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);

        if (isActive()) {
            guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                    getWidth(), getHeight(),
                    6, 6,
                    6, 6,
                    64, 20,
                    0, 0);
        }
        else {
            guiGraphics.blitNineSliced(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(),
                    getWidth(), getHeight(),
                    6, 6,
                    6, 6,
                    64, 20,
                    0, 20);
        }

        int color = ((int) (this.alpha * 255.0f) << 24) | 0xD9CAD5;
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, title, getX() + getWidth() / 2, getY() + (getHeight() / 2) - 3, color);
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
