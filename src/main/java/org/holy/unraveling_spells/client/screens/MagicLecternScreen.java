package org.holy.unraveling_spells.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
import org.holy.unraveling_spells.client.buttons.magic_screen.DefaultButton;

import static org.holy.unraveling_spells.client.screens.LearningScreen.MAIN_INV;

public class MagicLecternScreen extends AbstractContainerScreen<MagicLecternMenu> {
    private ResourceLocation MAINTEXTURE = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/magic_lectern_gui.png");
    int left = 0;
    int top = 0;
    MagicLecternTile blockEntity;

    public MagicLecternScreen(MagicLecternMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        left = (this.width - 256) / 2;
        top  = (this.height - 160) / 2;
        this.inventoryLabelY-= 1000;
        this.blockEntity = getMenu().blockEntity;

        DefaultButton LearningButton = new DefaultButton(left+40, top+40, minecraft.font, Component.empty(), MAINTEXTURE,
                0, 0, 80, 80,
                256, 256, 0, 80) {
            @Override
            public void onPress() {
                getMinecraft().setScreen(new LearningScreen(getMenu(), getMinecraft().player.getInventory(), Component.literal(" ")));
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_93660_) {
                super.renderWidget(guiGraphics, mouseX, mouseY, p_93660_);

                guiGraphics.blit(MAINTEXTURE, getX(), getY(), 80, 0, 80, 80, 256, 256);
                guiGraphics.drawString(font, Component.translatable("ui.unraveling_spells.button.learning"),
                        getX()+(getWidth()-font.width(Component.translatable("ui.unraveling_spells.button.learning").getString()))/2,
                        getY()+65, 0xD9CAD5, false);
            }
        };

        DefaultButton ScrollsButton = new DefaultButton(left+140, top+40, minecraft.font, Component.empty(), MAINTEXTURE,
                0, 0, 80, 80,
                256, 256, 0, 80) {
            @Override
            public void onPress() {
                //pass
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_93660_) {
                super.renderWidget(guiGraphics, mouseX, mouseY, p_93660_);

                guiGraphics.blit(MAINTEXTURE, getX(), getY(), 160, 0, 80, 80, 256, 256);
                guiGraphics.drawString(font, Component.translatable("ui.unraveling_spells.button.creating"),
                        getX()+(getWidth()-font.width(Component.translatable("ui.unraveling_spells.button.creating").getString()))/2,
                        getY()+65, 0xD9CAD5, false);
            }

        };
        addRenderableWidget(LearningButton);
        addRenderableWidget(ScrollsButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(guiGraphics, p_283661_, p_281248_, p_281886_);

        guiGraphics.blit(MAIN_INV, left-79, top-4, 0, 0, 74, 168);
        guiGraphics.blit(MAIN_INV, left + (256 - 192) / 2, top+165, 0, 192, 192, 26);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(LearningScreen.MAIN, left, top, 0, 0, 256, 160);
    }
}
