package org.holy.unraveling_spells.client.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.MagicLecternScreen;

public class BookmarkButton extends Button {
    private BookmarkType type;
    public BookmarkButton(int x, int y, BookmarkType type) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(16, 16));
        this.type = type;

        if (type == BookmarkType.SCROLLS) {
            setWidth(18);
            setHeight(19);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        if (type == BookmarkType.SCROLLS) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        112, 80,
                        18, 19);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        130, 80,
                        18, 28);
            }
        } else if (type == BookmarkType.RED) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        112, 112,
                        16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        128, 112,
                        16, 22);
            }
        } else if (type == BookmarkType.BLUE) {
            if (!isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        144, 112,
                        16, 16);
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(),
                        160, 112,
                        16, 22);
            }
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
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    public enum BookmarkType {
        SCROLLS,
        RED,
        BLUE
    }
}
