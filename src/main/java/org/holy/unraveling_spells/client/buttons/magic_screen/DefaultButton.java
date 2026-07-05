package org.holy.unraveling_spells.client.buttons.magic_screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.client.screens.LearningScreen;

public class DefaultButton extends Button {
    Font font;
    Component title;
    ResourceLocation textureLoc;
    int ux, uy, uw, uh, texW, texH;
    int uxIMO, uyIMO;

    public DefaultButton(int x, int y, Font font, Component title, ResourceLocation textureLoc, int ux, int uy, int uw, int uh, int texW, int texH) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(uw, uh));
        this.font = font;
        this.title = title;
        this.textureLoc = textureLoc;
        this.ux = ux;
        this.uy = uy;
        this.uw = uw;
        this.uh = uh;
        this.texW = texW;
        this.texH = texH;
        this.uxIMO = -1;
        this.uyIMO = -1;
    }
    public DefaultButton(int x, int y, Font font, Component title, ResourceLocation textureLoc, int ux, int uy, int uw, int uh, int texW, int texH, int uxIMO, int uyIMO) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(uw, uh));
        this.font = font;
        this.title = title;
        this.textureLoc = textureLoc;
        this.ux = ux;
        this.uy = uy;
        this.uw = uw;
        this.uh = uh;
        this.texW = texW;
        this.texH = texH;
        this.uxIMO = uxIMO;
        this.uyIMO = uyIMO;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_93660_) {
        if (uxIMO != -1 && uyIMO != -1) {
            if (isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(textureLoc, getX(), getY(), uxIMO, uyIMO, uw, uh, texW, texH);
            } else {
                guiGraphics.blit(textureLoc, getX(), getY(), ux, uy, uw, uh, texW, texH);
            }
        } else {
            guiGraphics.blit(textureLoc, getX(), getY(), ux, uy, uw, uh, texW, texH);
        }

        // title
        if (title != Component.empty()) guiGraphics.drawString(font, title, getX()+(64-font.width(title))/2, getY() + (getHeight() / 2) - 3, 0xD9CAD5, false);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
