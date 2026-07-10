package org.holy.unraveling_spells.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
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
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.SchoolS2CPacket;

import java.util.ArrayList;
import java.util.Set;

public class SchoolCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("school")
                .requires(source -> source.hasPermission(2)) // OP-уровень 2
                .then(Commands.literal("learn")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("school", ResourceLocationArgument.id())
                                        .suggests(SchoolSuggestionProvider.ALL_SCHOOLS)
                                        .executes(ctx -> learnSchool(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "school"),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("forget")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("school", ResourceLocationArgument.id())
                                        .suggests(SchoolSuggestionProvider.ALL_SCHOOLS)
                                        .executes(ctx -> forgetSchool(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "school"),
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

    private static int learnSchool(CommandSourceStack source, ResourceLocation schoolId, Player player) {
        // Проверяем, существует ли школа
        if (SchoolRegistry.getSchool(schoolId) == null) {
            source.sendFailure(Component.literal("This school does not exist!").withStyle(ChatFormatting.RED));
            return 0;
        }

        // Получаем capability игрока
        LazyOptional<PlayerSchool> cap = player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL);
        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSchool schoolData = cap.orElse(null);

        if (schoolData == null) {
            source.sendFailure(Component.literal("Failed to access player's school data.").withStyle(ChatFormatting.RED));
            return 0;
        }

        // Проверяем лимит школ
        if (schoolData.getSchools().size() >= Configuration.MAX_SCHOOLS.get()) {
            source.sendFailure(Component.literal("Player already has maximum number of schools (" + Configuration.MAX_SCHOOLS.get() + ")!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Добавляем школу
        schoolData.addSchool(schoolId);

        // Отправляем сообщение игроку и отправителю
        player.sendSystemMessage(Component.literal("You learned school: " + schoolId));
        source.sendSuccess(() -> Component.literal("School " + schoolId + " learned by " + player.getName().getString()), false);

        // Синхронизируем данные (если нужно — например, для клиента)
        ModMessages.sendToPlayer(
                new SchoolS2CPacket(new ArrayList<>(schoolData.getSchools())),
                (ServerPlayer) player
        );

        return 1;
    }

    private static int forgetSchool(CommandSourceStack source, ResourceLocation schoolId, Player player) {
        // Проверяем, есть ли такая школа у игрока
        LazyOptional<PlayerSchool> cap = player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL);

        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSchool schoolData = cap.orElse(null);

        if (schoolData == null || !schoolData.getSchools().contains(schoolId)) {
            source.sendFailure(Component.literal("Player does not know school: " + schoolId).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Удаляем школу
        schoolData.removeSchool(schoolId);

        // Сообщения
        player.sendSystemMessage(Component.literal("You forgot school: " + schoolId));
        source.sendSuccess(() -> Component.literal("School " + schoolId + " forgotten by " + player.getName().getString()), false);

        // Синхронизация (при необходимости)
        ModMessages.sendToPlayer(
                new SchoolS2CPacket(new ArrayList<>(schoolData.getSchools())),
                (ServerPlayer) player
        );

        return 1;
    }

    private static int getSchools(CommandContext<CommandSourceStack> ctx, Player player) {
        CommandSourceStack source = ctx.getSource();

        LazyOptional<PlayerSchool> cap = player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL);

        if (!cap.isPresent()) {
            source.sendFailure(Component.literal("Player capability not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSchool schoolData = cap.orElse(null);

        if (schoolData == null) {
            source.sendFailure(Component.literal("Failed to access player's school data.").withStyle(ChatFormatting.RED));
            return 0;
        }

        Set<ResourceLocation> schools = schoolData.getSchools();

        if (schools.isEmpty()) {
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " knows no schools."), false);
        } else {
            StringBuilder sb = new StringBuilder();

            for (ResourceLocation school : schools) {
                sb.append(school).append(", ");
            }

            String list = sb.substring(0, sb.length() - 2); // Убираем последнюю запятую
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " knows schools: " + list), false);
        }

        return 1;
    }
}