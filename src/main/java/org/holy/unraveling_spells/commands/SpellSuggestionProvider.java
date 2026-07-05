package org.holy.unraveling_spells.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public class SpellSuggestionProvider {

    public static final SuggestionProvider<CommandSourceStack> ALL_SPELLS =
            (context, builder) -> {

                SpellRegistry.getEnabledSpells().forEach(spell -> {
                    ResourceLocation id = spell.getSpellResource();
                    builder.suggest(id.toString());
                });

                return builder.buildFuture();
            };
}
