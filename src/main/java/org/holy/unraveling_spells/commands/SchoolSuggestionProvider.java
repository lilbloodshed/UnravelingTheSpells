package org.holy.unraveling_spells.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public class SchoolSuggestionProvider {
    public static final SuggestionProvider<CommandSourceStack> ALL_SCHOOLS =
            (context, builder) -> {

                SchoolRegistry.REGISTRY.get().getValues().stream().toList().forEach(schoolType -> {
                    ResourceLocation id = schoolType.getId();
                    builder.suggest(id.toString());
                });

                return builder.buildFuture();
            };
}
