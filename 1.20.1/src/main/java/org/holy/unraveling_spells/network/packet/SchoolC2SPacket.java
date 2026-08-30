package org.holy.unraveling_spells.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.network.ModMessages;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.function.Supplier;

public class SchoolC2SPacket {
    List<ResourceLocation> schools;

    public SchoolC2SPacket(List<ResourceLocation> schools) {
        this.schools = schools;
    }

    public SchoolC2SPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        schools = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            schools.add(ResourceLocation.parse(buf.readUtf()));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(schools.size());

        for (ResourceLocation school : schools) {
            buf.writeUtf(school.toString());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player != null) {
                player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).ifPresent(schoolData -> {
                    Set<ResourceLocation> requestedSchools = new LinkedHashSet<>(schools);
                    Set<ResourceLocation> knownSchools = new LinkedHashSet<>(schoolData.getSchools());
                    requestedSchools.removeAll(knownSchools);
                    int maximumSchools = getMaximumLearnableSchools();

                    if (requestedSchools.isEmpty()
                            || requestedSchools.size() > maximumSchools - knownSchools.size()
                            || (Configuration.SCHOOLS_PRICE_TYPE.get() == Configuration.LearnType.XP
                            && !Configuration.isSchoolXpBulkLearningAllowed() && requestedSchools.size() > 1)
                            || requestedSchools.stream().anyMatch(school -> !isLearnableSchool(school))) {
                        return;
                    }

                    if (Configuration.SCHOOLS_PRICE_TYPE.get() == Configuration.LearnType.XP) {
                        if (player.experienceLevel < Configuration.XP_MINIMUM_LEVEL.get()) {
                            return;
                        }

                        int xpCost;
                        try {
                            xpCost = Configuration.getSchoolXpCost(requestedSchools, knownSchools.size());
                        } catch (ArithmeticException exception) {
                            return;
                        }
                        if (player.totalExperience < xpCost) {
                            return;
                        }
                        player.giveExperiencePoints(-xpCost);
                    } else if (requestedSchools.size() != maximumSchools - knownSchools.size()) {
                        return;
                    }

                    schoolData.getSchools().addAll(requestedSchools);

                    ModMessages.sendToPlayer(new SchoolS2CPacket(new ArrayList<>(schoolData.getSchools())), player);
                    ModMessages.sendToPlayer(new SetTotalPlayerXPPacket(player.totalExperience), player);
                });
            }
        });
        return true;
    }

    private static boolean isLearnableSchool(ResourceLocation schoolId) {
        return SchoolRegistry.REGISTRY.get().getValues().stream()
                .anyMatch(school -> school.getId().equals(schoolId))
                && !Configuration.isSchoolLearningDisabled(schoolId)
                && (Configuration.ENABLE_ELDRITCH_SCHOOL_LEARNING.get()
                || !SchoolRegistry.ELDRITCH.get().getId().equals(schoolId));
    }

    private static int getMaximumLearnableSchools() {
        if (Configuration.isMaxSchoolsLimitEnabled()) {
            return Configuration.getMaxSchools();
        }
        return (int) SchoolRegistry.REGISTRY.get().getValues().stream()
                .filter(school -> isLearnableSchool(school.getId()))
                .count();
    }
}
