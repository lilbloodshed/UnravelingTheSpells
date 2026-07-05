package org.holy.unraveling_spells.client.buttons.magic_screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.LearningScreen;

public class RightLevelButton extends Button{
    public RightLevelButton(int x, int y) {
        super(new Button.Builder(Component.literal(""), null)
                .pos(x, y)
                .size(7, 7));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 89, 41, 7, 7);
        } else {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 89, 32, 7, 7);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
