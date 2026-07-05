package org.holy.unraveling_spells.client.buttons.magic_screen;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.Unraveling_spells;

public class SpellButton extends Button {
    AbstractSpell spell;
    boolean isLearned;
    boolean isCurrentSpell;
    boolean isBlocked;

    public SpellButton(int x, int y, AbstractSpell spell, boolean isLearned, boolean isBlocked, boolean isCurrentSpell) {
        super(new Button.Builder(Component.literal(""), null)
                .pos(x, y)
                .size(20, 20));
        this.spell = spell;
        this.isLearned = isLearned;
        this.isBlocked = isBlocked;
        this.isCurrentSpell = isCurrentSpell;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_282542_) {
        if (!isBlocked) {
            if (isLearned) {
                if (isCurrentSpell) {
                    guiGraphics.blit(spell.getSpellIconResource(), getX()+1, getY()+1, 0, 0, 18, 18, 18, 18);
                    guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_learned_border_active.png"),
                            getX()-1, getY()-1, 0, 0, 22, 22, 22, 22);
                } else {
                    guiGraphics.blit(spell.getSpellIconResource(), getX()+2, getY()+2, 0, 0, 16, 16, 16, 16);
                    if (isMouseOver(mouseX, mouseY)) guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_learned_border1.png"),
                            getX(), getY(), 0, 0, 20, 20, 20, 20);
                    else guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_learned_border0.png"),
                            getX(), getY(), 0, 0, 20, 20, 20, 20);
                }
            } else {
                if (isCurrentSpell) {
                    guiGraphics.blit(spell.getSpellIconResource(), getX()+1, getY()+1, 0, 0, 18, 18, 18, 18);
                    guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_border_active.png"),
                            getX()-1, getY()-1, 0, 0, 22, 22, 22, 22);
                } else {
                    guiGraphics.blit(spell.getSpellIconResource(), getX()+2, getY()+2, 0, 0, 16, 16, 16, 16);
                    if (isMouseOver(mouseX, mouseY)) guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_border1.png"),
                            getX(), getY(), 0, 0, 20, 20, 20, 20);
                    else guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_border0.png"),
                            getX(), getY(), 0, 0, 20, 20, 20, 20);
                }
            }
        } else {
            guiGraphics.blit(spell.getSpellIconResource(), getX()+2, getY()+2, 0, 0, 16, 16, 16, 16);
            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/spell_blocked.png"),
                    getX(), getY(), 0, 0, 20, 20, 20, 20);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
