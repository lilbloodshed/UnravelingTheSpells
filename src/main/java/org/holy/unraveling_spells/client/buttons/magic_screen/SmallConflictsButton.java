package org.holy.unraveling_spells.client.buttons.magic_screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.LearningScreen;

public class SmallConflictsButton extends Button {
    boolean isMouseOn;
    boolean isBlocked;
    private Component tooltip;
    Font font;

    public SmallConflictsButton(int x, int y, Font font, boolean isBlocked) {
        super(new Button.Builder(Component.literal(""), null)
                .pos(x, y)
                .size(14, 14));
        this.isBlocked = isBlocked;
        this.font = font;

        tooltip = Component.translatable("ui.unraveling_spells.tooltip.conflicts");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        isMouseOn = isMouseOver(mouseX, mouseY);

        if (isMouseOn && !isBlocked) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 64, 16, 14, 14);
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else if (isBlocked) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 80, 0, 14, 14);
            if (isMouseOn) guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 64, 0, 14, 14);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
