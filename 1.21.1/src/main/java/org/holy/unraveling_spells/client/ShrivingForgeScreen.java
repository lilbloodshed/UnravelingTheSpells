package org.holy.unraveling_spells.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeMenu;

public class ShrivingForgeScreen extends AbstractContainerScreen<ShrivingForgeMenu> {
    private static final ResourceLocation TEXTURE_BG = ResourceLocation.fromNamespaceAndPath(
            "unraveling_spells", "textures/gui/shriving_forge_gui.png");
    private static final ResourceLocation TEXTURE_PROGRESS = ResourceLocation.fromNamespaceAndPath(
            "unraveling_spells", "textures/gui/shriving_forge_progress_circle.png");
    private static final ResourceLocation TEXTURE_INV = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/gui/container/smithing.png");
    private float displayedProgress;

    public ShrivingForgeScreen(ShrivingForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        inventoryLabelY -= 1000;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderProgressCircle(GuiGraphics graphics) {
        int circleX = leftPos + imageWidth / 2 - 34;
        int circleY = topPos + 5;
        displayedProgress = Mth.lerp(0.2F, displayedProgress, menu.getProgress());
        graphics.blit(TEXTURE_BG, circleX, circleY, 0, 80, 68, 68);

        int fillHeight = Mth.ceil(68.0F * displayedProgress);
        if (fillHeight <= 0) return;

        graphics.enableScissor(circleX, circleY + 68 - fillHeight, circleX + 68, circleY + 68);
        graphics.blit(TEXTURE_PROGRESS, circleX, circleY, 0, 0, 68, 68, 68, 68);
        graphics.disableScissor();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE_INV, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE_BG, leftPos, topPos, 0, 0, 176, 79);
        renderProgressCircle(graphics);
    }
}
