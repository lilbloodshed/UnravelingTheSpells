package org.holy.unraveling_spells.client.buttons.magic_screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.LearningScreen;
import org.jetbrains.annotations.NotNull;

public class SmallLearnButton extends Button{
    boolean isMouseOn;
    boolean isBlocked;
    boolean isLearned;
    private Component tooltip;
    Font font;

    public SmallLearnButton(int x, int y, Font font, boolean isBlocked, boolean isLearned) {
        super(new Button.Builder(Component.literal(""), null)
                .pos(x, y)
                .size(14, 14));
        this.isBlocked = isBlocked;
        this.font = font;
        this.isLearned = isLearned;

        updateTooltip();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        isMouseOn = isMouseOver(mouseX, mouseY);

        if (isMouseOn && !isBlocked && !isLearned) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 0, 16, 14, 14);
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else if (isBlocked && !isLearned) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 16, 0, 14, 14);
            if (isMouseOn) guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else if (isLearned) {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 16, 16, 14, 14);
        } else {
            guiGraphics.blit(LearningScreen.ICONS, getX(), getY(), 0, 0, 14, 14);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    private void updateTooltip() {
        if (isBlocked) {
            tooltip = Component.translatable("ui.unraveling_spells.tooltip.cannot_learn")
                    .withStyle(ChatFormatting.RED);
        } else {
            tooltip = Component.translatable("ui.unraveling_spells.tooltip.can_learn")
                    .withStyle(ChatFormatting.GREEN);
        }
    }
}
