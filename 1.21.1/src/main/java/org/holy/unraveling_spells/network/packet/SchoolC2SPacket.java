package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record SchoolC2SPacket(List<ResourceLocation> schools) implements CustomPacketPayload {
    public static final Type<SchoolC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "school_c2s"));
    public static final StreamCodec<ByteBuf, SchoolC2SPacket> STREAM_CODEC =
            PacketCodecs.RESOURCE_LOCATION_LIST.map(SchoolC2SPacket::new, SchoolC2SPacket::schools);

    public SchoolC2SPacket {
        schools = List.copyOf(schools);
    }

    public static void handle(SchoolC2SPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        var schoolData = PlayerSchoolProvider.get(player);
        Set<ResourceLocation> knownSchools = new LinkedHashSet<>(schoolData.getSchools());
        int maximumSchools = getMaximumLearnableSchools();
        schoolData.getSchools().clear();
        schoolData.getSchools().addAll(packet.schools());

        if (Configuration.SCHOOLS_PRICE_TYPE.get() == Configuration.LearnType.XP) {
            if (player.experienceLevel < Configuration.XP_MINIMUM_LEVEL.get()) {
                return;
            }

            int xpCost;
            try {
                xpCost = Configuration.getSchoolXpCost(packet.schools, knownSchools.size());
            } catch (ArithmeticException exception) {
                return;
            }
            if (player.totalExperience < xpCost) {
                return;
            }
            player.giveExperiencePoints(-xpCost);
        } else if (packet.schools.size() != maximumSchools - knownSchools.size()) {
            return;
        }

        ModMessages.sendToPlayer(new SchoolS2CPacket(packet.schools()), player);
        ModMessages.sendToPlayer(new SetTotalPlayerXPPacket(player.totalExperience), player);
    }

    private static boolean isLearnableSchool(ResourceLocation schoolId) {
        return SchoolRegistry.REGISTRY.stream().anyMatch(school -> school.getId().equals(schoolId))
                && !Configuration.isSchoolLearningDisabled(schoolId)
                && (Configuration.ENABLE_ELDRITCH_SCHOOL_LEARNING.get()
                || !SchoolRegistry.ELDRITCH.get().getId().equals(schoolId));
    }

    private static int getMaximumLearnableSchools() {
        if (Configuration.isMaxSchoolsLimitEnabled()) {
            return Configuration.getMaxSchools();
        }
        return (int) SchoolRegistry.REGISTRY.stream().filter(school ->
                        isLearnableSchool(school.getId())).count();
    }

    @Override
    public Type<SchoolC2SPacket> type() {
        return TYPE;
    }
}
