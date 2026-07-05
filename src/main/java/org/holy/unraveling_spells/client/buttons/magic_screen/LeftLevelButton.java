package org.holy.unraveling_spells.client.buttons.magic_screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.LearningScreen;

public class LeftLevelButton extends Button {
    public LeftLevelButton(int x, int y) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(7, 7));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_282542_) {
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 80, 41, 7, 7);
        } else {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 80, 32, 7, 7);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
