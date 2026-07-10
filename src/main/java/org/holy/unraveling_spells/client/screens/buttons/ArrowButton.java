package org.holy.unraveling_spells.client.screens.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;

public class ArrowButton extends Button {
    private String direction;
    public ArrowButton(int x, int y, String direction) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(16, 16));
        this.direction = direction;

        int sizeW = direction.equals("left") || direction.equals("right") ? 9 : 16;
        int sizeH = direction.equals("left") || direction.equals("right") ? 16 : 9;
        this.setWidth(sizeW);
        this.setHeight(sizeH);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        switch (direction) {
            case "left":
                leftDirection(guiGraphics, mouseX, mouseY);
                break;
            case "right":
                rightDirection(guiGraphics, mouseX, mouseY);
                break;
            case "up":
                upDirection(guiGraphics, mouseX, mouseY);
                break;
            case "down":
                downDirection(guiGraphics, mouseX, mouseY);
                break;
        }
    }

    private void leftDirection(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isActive()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 128, 32, 16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 128, 48, 16, 16);
            }
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 128, 64, 16, 16);
        }
    }

    private void rightDirection(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isActive()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 144, 32, 16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 144, 48, 16, 16);
            }
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 144, 64, 16, 16);
        }
    }

    private void upDirection(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isActive()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 96, 32, 16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 96, 48, 16, 16);
            }
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 96, 64, 16, 16);
        }
    }

    private void downDirection(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isActive()) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 112, 32, 16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 112, 48, 16, 16);
            }
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS, getX(), getY(), 112, 64, 16, 16);
        }
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
