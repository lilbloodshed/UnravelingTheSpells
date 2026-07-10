package org.holy.unraveling_spells.client.buttons_old.magic_screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DefaultButton extends Button {
    Font font;
    Component title;
    ResourceLocation textureLoc;
    int ux, uy, uw, uh, texW, texH;
    int uxIMO, uyIMO;
    int displayWidth, displayHeight;

    public DefaultButton(int x, int y, Font font, Component title, ResourceLocation textureLoc, int ux, int uy, int uw, int uh, int texW, int texH) {
        super(new Builder(Component.literal(""), button -> {})
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
        this.displayWidth = uw;
        this.displayHeight = uh;
    }
    public DefaultButton(int x, int y, Font font, Component title, ResourceLocation textureLoc, int ux, int uy, int uw, int uh, int texW, int texH, int uxIMO, int uyIMO) {
        super(new Builder(Component.literal(""), button -> {})
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
        this.displayWidth = uw;
        this.displayHeight = uh;
    }

    public DefaultButton(int x, int y, int displayWidth, int displayHeight, Font font, Component title, ResourceLocation textureLoc,
                         int ux, int uy, int uw, int uh, int texW, int texH, int uxIMO, int uyIMO) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(displayWidth, displayHeight));
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
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_93660_) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0);
        guiGraphics.pose().scale((float) displayWidth / uw, (float) displayHeight / uh, 1.0f);
        if (uxIMO != -1 && uyIMO != -1) {
            if (isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(textureLoc, 0, 0, uxIMO, uyIMO, uw, uh, texW, texH);
            } else {
                guiGraphics.blit(textureLoc, 0, 0, ux, uy, uw, uh, texW, texH);
            }
        } else {
            guiGraphics.blit(textureLoc, 0, 0, ux, uy, uw, uh, texW, texH);
        }
        guiGraphics.pose().popPose();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        // title
        if (title != Component.empty()) {
            int color = ((int) (this.alpha * 255.0f) << 24) | 0xD9CAD5;
            guiGraphics.drawString(font, title, getX()+(getWidth()-font.width(title))/2, getY() + (getHeight() / 2) - 3, color, false);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
