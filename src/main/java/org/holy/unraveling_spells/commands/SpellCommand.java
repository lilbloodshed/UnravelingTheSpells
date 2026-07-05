package org.holy.unraveling_spells.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.config.SpellLearnedManager;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.SpellS2CPacket;

import java.util.ArrayList;
import java.util.Set;

public class SpellCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spell")
                .requires(source -> source.hasPermission(2)) // OP-уровень 2
                .then(Commands.literal("learn")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("spell", ResourceLocationArgument.id())
                                        .suggests(SpellSuggestionProvider.ALL_SPELLS)
                                        .executes(ctx -> learnSchool(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "spell"),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("forget")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("spell", ResourceLocationArgument.id())
                                        .suggests(SpellSuggestionProvider.ALL_SPELLS)
                                        .executes(ctx -> forgetSchool(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "spell"),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> getSchools(
                                        ctx,
                                        EntityArgument.getPlayer(ctx, "player")
                                ))
                        )
                )
        );
    }

    private static int learnSchool(CommandSourceStack source, ResourceLocation spellId, Player player) {
        // Проверяем, существует ли школа
        if (SpellRegistry.getSpell(spellId) == null) {
            source.sendFailure(Component.literal("Spell not found: " + spellId).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Получаем capability игрока
        LazyOptional<PlayerSpell> cap = player.getCapability(PlayerSpellProvider.PLAYER_SPELL);

        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSpell spellData = cap.orElse(null);

        if (spellData == null) {
            source.sendFailure(Component.literal("Failed to access player's spell data.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!SpellLearnedManager.isSpellDefaultLearned(spellId)) {
            spellData.addSpell(spellId);

            player.sendSystemMessage(Component.literal("You learned spell: " + spellId));
            source.sendSuccess(() -> Component.literal("Spell " + spellId + " learned by " + player.getName().getString()), false);

            ModMessages.sendToPlayer(
                    new SpellS2CPacket(new ArrayList<>(spellData.getSpells())),
                    (ServerPlayer) player
            );
        }
        else {
            source.sendSuccess(() -> Component.literal("Spell " + spellId + " is learned by default!"), false);
        }

        return 1;
    }

    private static int forgetSchool(CommandSourceStack source, ResourceLocation spellId, Player player) {
        // Проверяем, есть ли такая школа у игрока
        LazyOptional<PlayerSpell> cap = player.getCapability(PlayerSpellProvider.PLAYER_SPELL);

        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSpell spellData = cap.orElse(null);

        if (spellData == null || !spellData.getSpells().contains(spellId)) {
            source.sendFailure(Component.literal("Player does not know spell: " + spellId).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!SpellLearnedManager.isSpellDefaultLearned(spellId)) {
            spellData.removeSpell(spellId);

            player.sendSystemMessage(Component.literal("You forgot spell: " + spellId));
            source.sendSuccess(() -> Component.literal("Spell " + spellId + " forgotten by " + player.getName().getString()), false);

            ModMessages.sendToPlayer(
                    new SpellS2CPacket(new ArrayList<>(spellData.getSpells())),
                    (ServerPlayer) player
            );
        }
        else {
            source.sendSuccess(() -> Component.literal("Spell " + spellId + " is learned by default!"), false);
        }

        return 1;
    }

    private static int getSchools(CommandContext<CommandSourceStack> ctx, Player player) {
        CommandSourceStack source = ctx.getSource();

        LazyOptional<PlayerSpell> cap = player.getCapability(PlayerSpellProvider.PLAYER_SPELL);

        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSpell spellData = cap.orElse(null);

        if (spellData == null) {
            source.sendFailure(Component.literal("Failed to access player's spell data.").withStyle(ChatFormatting.RED));
            return 0;
        }

        Set<ResourceLocation> spells = spellData.getSpells();

        if (spells.isEmpty()) {
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " knows no spells."), false);
        } else {
            StringBuilder sb = new StringBuilder();

            for (ResourceLocation spell : spells) {
                sb.append(spell).append(", ");
            }

            String list = sb.substring(0, sb.length() - 2); // Убираем последнюю запятую
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " knows spells: " + list), false);
        }

        return 1;
    }
}
