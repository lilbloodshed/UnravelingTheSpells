package org.holy.unraveling_spells.client.screens.buttons;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;

public class SpellButton extends Button {
    private AbstractSpell spell;
    private boolean isSelected = false;
    private boolean isLearned = false;
    private boolean isBlocked = false;

    public SpellButton(int x, int y, AbstractSpell spell) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(20, 20));
        this.spell = spell;
    }

    public AbstractSpell getSpell() { return spell; }

    public boolean isSelected() { return isSelected; }

    public boolean isLearned() { return isLearned; }

    public boolean isBlocked() { return isBlocked; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        guiGraphics.blit(spell.getSpellIconResource(),
                getX()+2, getY()+2,
                0, 0,
                16,16,
                16,16);

        renderBorder(guiGraphics, mouseX, mouseY, ticks);
    }

    private void renderBorder(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        if (!isBlocked) {
            if (!isLearned) {
                if (!isSelected) {
                    if (!isMouseOver(mouseX, mouseY)) {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                0, 128,
                                20,20);
                    } else {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                0, 148,
                                20,20);
                    }
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(),
                            0, 168,
                            20,20);
                }
            } else {
                if (!isSelected) {
                    if (!isMouseOver(mouseX, mouseY)) {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                20, 128,
                                20,20);
                    } else {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                20, 148,
                                20,20);
                    }
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(),
                            20, 168,
                            20,20);
                }
            }
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                    getX(), getY(),
                    40, 148,
                    20,20);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isActive() && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return isActive() && super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (isActive()) {
            super.playDownSound(soundManager);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isActive() && mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
