package org.holy.unraveling_spells.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.PlayerSpellProvider;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.config.SpellLearnedManager;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.SchoolS2CPacket;
import org.holy.unraveling_spells.network.packet.SpellS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class UTSCommand {
    private UTSCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("uts")
                .requires(source -> source.hasPermission(2))
                .then(createBranch(KnowledgeType.SPELL))
                .then(createBranch(KnowledgeType.SCHOOL)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createBranch(KnowledgeType type) {
        return Commands.literal(type.literal)
                .then(Commands.literal("learn")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument(type.argumentName, resourceLocationArgument())
                                        .suggests((context, builder) -> suggestIds(context, builder, type, false))
                                        .executes(context -> changeOne(context, type, true)))))
                .then(Commands.literal("learnALL")
                        .executes(context -> changeAll(
                                context.getSource(),
                                context.getSource().getPlayerOrException(),
                                type,
                                true))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> changeAll(context, type, true))))
                .then(Commands.literal("forget")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument(type.argumentName, resourceLocationArgument())
                                        .suggests((context, builder) -> suggestIds(context, builder, type, true))
                                        .executes(context -> changeOne(context, type, false)))))
                .then(Commands.literal("forgetALL")
                        .executes(context -> changeAll(
                                context.getSource(),
                                context.getSource().getPlayerOrException(),
                                type,
                                false))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> changeAll(context, type, false))))
                .then(Commands.literal("get")
                        .executes(context -> showLearned(
                                context.getSource(),
                                context.getSource().getPlayerOrException(),
                                type))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> showLearned(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        type))));
    }

    private static ArgumentType<ResourceLocation> resourceLocationArgument() {
        return ResourceLocationArgument.id();
    }

    private static CompletableFuture<Suggestions> suggestIds(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder,
            KnowledgeType type,
            boolean learnedOnly) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        Set<ResourceLocation> learned = learnedIds(player, type);
        if (learned == null) {
            return builder.buildFuture();
        }

        Stream<ResourceLocation> suggestions = learnedOnly
                ? learned.stream().filter(id -> canForget(type, id))
                : allIds(type).filter(id -> !isLearned(type, learned, id));
        return SharedSuggestionProvider.suggestResource(suggestions, builder);
    }

    private static int changeOne(CommandContext<CommandSourceStack> context, KnowledgeType type, boolean learn)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = ResourceLocationArgument.getId(context, type.argumentName);

        if (!exists(type, id)) {
            source.sendFailure(Component.literal(type.displayName + " does not exist: " + id)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Set<ResourceLocation> learned = learnedIds(player, type);
        if (learned == null) {
            return unavailable(source, type);
        }

        if (learn) {
            if (isLearned(type, learned, id)) {
                source.sendFailure(Component.literal(type.displayName + " is already learned: " + id)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            learned.add(id);
        } else {
            if (!canForget(type, id)) {
                source.sendFailure(Component.literal(type.displayName + " is learned by default: " + id)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            if (!learned.remove(id)) {
                source.sendFailure(Component.literal(type.displayName + " is not learned: " + id)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        sync(player, type, learned);
        String action = learn ? "learned" : "forgotten";
        source.sendSuccess(() -> Component.literal(
                type.displayName + " " + id + " " + action + " by " + player.getName().getString()), true);
        player.sendSystemMessage(Component.literal("You " + action + " " + type.displayName.toLowerCase() + ": " + id));
        return 1;
    }

    private static int changeAll(CommandContext<CommandSourceStack> context, KnowledgeType type, boolean learn)
            throws CommandSyntaxException {
        return changeAll(
                context.getSource(),
                EntityArgument.getPlayer(context, "player"),
                type,
                learn);
    }

    private static int changeAll(
            CommandSourceStack source,
            ServerPlayer player,
            KnowledgeType type,
            boolean learn) {
        Set<ResourceLocation> learned = learnedIds(player, type);
        if (learned == null) {
            return unavailable(source, type);
        }

        int changed;
        if (learn) {
            Set<ResourceLocation> additions = new LinkedHashSet<>();
            allIds(type)
                    .filter(id -> !isLearned(type, learned, id))
                    .forEach(additions::add);
            learned.addAll(additions);
            changed = additions.size();
        } else {
            changed = (int) learned.stream().filter(id -> canForget(type, id)).count();
            learned.removeIf(id -> canForget(type, id));
        }

        sync(player, type, learned);
        int result = changed;
        String action = learn ? "learned" : "forgotten";
        source.sendSuccess(() -> Component.literal(
                result + " " + type.literal + " entries " + action + " by " + player.getName().getString()), true);
        return changed;
    }

    @Nullable
    private static Set<ResourceLocation> learnedIds(ServerPlayer player, KnowledgeType type) {
        if (type == KnowledgeType.SPELL) {
            PlayerSpell spells = player.getCapability(PlayerSpellProvider.PLAYER_SPELL).orElse(null);
            return spells == null ? null : spells.getSpells();
        }
        PlayerSchool schools = player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).orElse(null);
        return schools == null ? null : schools.getSchools();
    }

    private static Stream<ResourceLocation> allIds(KnowledgeType type) {
        if (type == KnowledgeType.SPELL) {
            return SpellRegistry.getEnabledSpells().stream().map(spell -> spell.getSpellResource());
        }
        return SchoolRegistry.REGISTRY.get().getValues().stream().map(school -> school.getId());
    }

    private static int showLearned(
            CommandSourceStack source,
            ServerPlayer player,
            KnowledgeType type) {
        Set<ResourceLocation> learned = learnedIds(player, type);
        if (learned == null) {
            return unavailable(source, type);
        }

        Set<ResourceLocation> displayed = new LinkedHashSet<>(learned);
        if (type == KnowledgeType.SPELL) {
            displayed.addAll(SpellLearnedManager.getDefaultLearnedSpells());
        }

        String playerName = player.getName().getString();
        if (displayed.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    playerName + " has no learned " + type.literal + " entries."), false);
            return 1;
        }

        String values = displayed.stream()
                .sorted()
                .map(ResourceLocation::toString)
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal(
                playerName + " learned " + type.literal + " entries (" + displayed.size() + "): " + values), false);
        return displayed.size();
    }

    private static boolean exists(KnowledgeType type, ResourceLocation id) {
        return type == KnowledgeType.SPELL
                ? SpellRegistry.getSpell(id) != null
                : SchoolRegistry.getSchool(id) != null;
    }

    private static boolean isLearned(
            KnowledgeType type,
            Set<ResourceLocation> learned,
            ResourceLocation id) {
        return learned.contains(id)
                || type == KnowledgeType.SPELL && SpellLearnedManager.isSpellDefaultLearned(id);
    }

    private static boolean canForget(KnowledgeType type, ResourceLocation id) {
        return type != KnowledgeType.SPELL || !SpellLearnedManager.isSpellDefaultLearned(id);
    }

    private static void sync(ServerPlayer player, KnowledgeType type, Set<ResourceLocation> learned) {
        if (type == KnowledgeType.SPELL) {
            ModMessages.sendToPlayer(new SpellS2CPacket(new ArrayList<>(learned)), player);
        } else {
            ModMessages.sendToPlayer(new SchoolS2CPacket(new ArrayList<>(learned)), player);
        }
    }

    private static int unavailable(CommandSourceStack source, KnowledgeType type) {
        source.sendFailure(Component.literal("Player " + type.literal + " data is unavailable")
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    private enum KnowledgeType {
        SPELL("spell", "spell", "Spell"),
        SCHOOL("school", "school", "School");

        private final String literal;
        private final String argumentName;
        private final String displayName;

        KnowledgeType(String literal, String argumentName, String displayName) {
            this.literal = literal;
            this.argumentName = argumentName;
            this.displayName = displayName;
        }
    }
}
