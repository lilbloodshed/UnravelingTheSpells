package org.holy.unraveling_spells.client.buttons_old.magic_screen;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SchoolButtonTest extends Button {
    SchoolType school;

    public SchoolButtonTest(int x, int y, SchoolType school) {
        super(new Builder(Component.literal(""), null)
                .pos(x, y)
                .size(50, 64));
        this.school = school;
    }
}
