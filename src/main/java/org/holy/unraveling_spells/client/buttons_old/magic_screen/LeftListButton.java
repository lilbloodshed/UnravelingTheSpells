package org.holy.unraveling_spells.client.buttons_old.magic_screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.LearningScreenOld;

public class LeftListButton extends Button {
    public LeftListButton(int x, int y) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(9, 15));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.blit(LearningScreenOld.ICONS, getX(), getY(), 32, 48, 16, 16);
        } else {
            guiGraphics.blit(LearningScreenOld.ICONS, getX(), getY(), 32, 32, 16, 16);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
