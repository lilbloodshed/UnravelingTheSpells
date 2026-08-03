package org.holy.unraveling_spells.client.screens.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
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

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isLearned() { return isLearned; }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public boolean isBlocked() { return isBlocked; }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);

        if (getSpell() != null) {
            guiGraphics.blit(getSpell().getSpellIconResource(),
                    getX()+2, getY()+2 - (isMouseOver(mouseX, mouseY) ? 1 : 0),
                    0, 0,
                    16,16,
                    16,16);
        } else {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                    getX()+2, getY()+2,
                    42, 172,
                    16,16);
        }

        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        renderBorder(guiGraphics, mouseX, mouseY, ticks);
        guiGraphics.flush();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBorder(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        boolean blocked = isBlocked();
        boolean learned = isLearned();
        boolean selected = isSelected();

        if (getSpell() == null) {
            guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                    getX(), getY(),
                    0, 170,
                    20,20);
            return;
        }

        if (!blocked) {
            if (!learned) {
                if (!selected) {
                    if (!isMouseOver(mouseX, mouseY)) {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                0, 128,
                                20,20);
                    } else {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY() - 1,
                                0, 148,
                                20,22);
                    }
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(),
                            0, 170,
                            20,20);
                }
            } else {
                if (!selected) {
                    if (!isMouseOver(mouseX, mouseY)) {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY(),
                                20, 128,
                                20,20);
                    } else {
                        guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                                getX(), getY() - 1,
                                20, 148,
                                20,22);
                    }
                } else {
                    guiGraphics.blit(MagicLecternScreen.TEXTURE_BUTTONS,
                            getX(), getY(),
                            20, 170,
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
        return getSpell() != null && isActive() && !isSelected() && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return getSpell() != null && isActive() && !isSelected() && super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (getSpell() != null && isActive() && !isSelected()) {
            super.playDownSound(soundManager);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return getSpell() != null && isActive() && !isSelected()
                && mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
