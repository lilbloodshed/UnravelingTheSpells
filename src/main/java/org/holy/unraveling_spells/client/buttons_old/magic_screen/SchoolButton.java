package org.holy.unraveling_spells.client.buttons_old.magic_screen;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SchoolButton extends Button {
    SchoolType school;

    public SchoolButton(int x, int y, SchoolType school) {
        super(new Builder(Component.literal(""), button -> {})
                .pos(x, y)
                .size(64, 48));
        this.school = school;
    }
}
