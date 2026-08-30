package org.holy.unraveling_spells.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeMenu;

public class ShrivingForgeScreen extends AbstractContainerScreen<ShrivingForgeMenu> {
    public static final ResourceLocation TEXTURE_BG = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/shriving_forge_gui.png");
    public static final ResourceLocation TEXTURE_PROGRESS = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/shriving_forge_progress_circle.png");
    public static final ResourceLocation TEXTURE_INV = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/smithing.png");
    int left, top;
    private float displayedProgress;

    public ShrivingForgeScreen(ShrivingForgeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        left = (this.width - 176) / 2;
        top = (this.height - 166) / 2;

        this.inventoryLabelY-= 1000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, ticks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderProgressCircle(GuiGraphics guiGraphics) {
        int circleX = left + (176 / 2) - 34;
        int circleY = top + 5;
        displayedProgress = Mth.lerp(0.2f, displayedProgress, menu.getProgress());

        guiGraphics.blit(TEXTURE_BG, circleX, circleY, 0, 80, 68, 68);

        int fillHeight = Mth.ceil(69.0f * displayedProgress);
        if (fillHeight <= 0) return;

        guiGraphics.enableScissor(circleX, circleY + 69 - fillHeight, circleX + 68, circleY + 69);
        guiGraphics.blit(TEXTURE_PROGRESS, circleX, circleY, 0, 0, 68, 68, 68, 68);
        guiGraphics.disableScissor();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(TEXTURE_INV, left, top, 0, 0, 176, 166);
        guiGraphics.blit(TEXTURE_BG, left, top, 0, 0, 176, 79);
        renderProgressCircle(guiGraphics);
    }
}
