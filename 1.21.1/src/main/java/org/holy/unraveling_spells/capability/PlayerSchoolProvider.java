package org.holy.unraveling_spells.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.jetbrains.annotations.Nullable;

/**
 * Registers and exposes the player's learned-school attachment.
 */
public final class PlayerSchoolProvider {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    Unraveling_spells.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSchool>> PLAYER_SCHOOL =
            ATTACHMENT_TYPES.register("properties_school", () ->
                    AttachmentType.builder(PlayerSchool::new)
                            .serialize(new SchoolSerializer())
                            .copyOnDeath()
                            .copyHandler((school, holder, registries) -> {
                                PlayerSchool copy = new PlayerSchool();
                                copy.copyFrom(school);
                                return copy;
                            })
                            .build());

    private PlayerSchoolProvider() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static PlayerSchool get(Player player) {
        return player.getData(PLAYER_SCHOOL);
    }

    private static final class SchoolSerializer
            implements IAttachmentSerializer<CompoundTag, PlayerSchool> {
        @Override
        public PlayerSchool read(
                IAttachmentHolder holder,
                CompoundTag tag,
                HolderLookup.Provider registries) {
            PlayerSchool schools = new PlayerSchool();
            schools.loadNBTData(tag);
            return schools;
        }

        @Nullable
        @Override
        public CompoundTag write(PlayerSchool schools, HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            schools.saveNBTData(tag);
            return tag;
        }
    }
}
