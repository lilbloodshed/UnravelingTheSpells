package org.holy.unraveling_spells.client.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.MagicLecternScreen;

public class MiniButtons extends Button {
    private MiniType type;
    public MiniButtons(int x, int y, MiniType type) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(16, 16));
        this.type = type;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);

        if (type == MiniType.LEARN) {
            if (isActive()) {
                if (!isMouseOver(mouseX, mouseY)) {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 96, 0, 14, 14);
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 96, 16, 14, 14);
                }
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(), 112, 0, 14, 14);
            }
        } else if (type == MiniType.DESCRIPTION) {
            if (isActive()) {
                if (!isMouseOver(mouseX, mouseY)) {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 128, 0, 14, 14);
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 128, 16, 14, 14);
                }
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(), 144, 0, 14, 14);
            }
        } else if (type == MiniType.CONFLICTS) {
            if (isActive()) {
                if (!isMouseOver(mouseX, mouseY)) {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 160, 0, 14, 14);
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 160, 16, 14, 14);
                }
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(), 176, 0, 14, 14);
            }
        } else if (type == MiniType.CHARACTERISTIC) {
            if (isActive()) {
                if (!isMouseOver(mouseX, mouseY)) {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 192, 0, 14, 14);
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(), 192, 16, 14, 14);
                }
            } else {
                guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                        getX(), getY(), 208, 0, 14, 14);
            }
        }

        guiGraphics.flush();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();
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

    public enum MiniType {
        LEARN,
        DESCRIPTION,
        CONFLICTS,
        CHARACTERISTIC
    }
}
