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
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.jetbrains.annotations.Nullable;

public class PlayerSpellProvider {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    Unraveling_spells.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSpell>> PLAYER_SPELL =
            ATTACHMENT_TYPES.register("properties_spell", () ->
                    AttachmentType.builder(PlayerSpell::new)
                            .serialize(new PlayerSpellProvider.SpellSerializer())
                            .copyOnDeath()
                            .copyHandler((spell, holder, registries) -> {
                                PlayerSpell copy = new PlayerSpell();
                                copy.copyFrom(spell);
                                return copy;
                            })
                            .build());

    private PlayerSpellProvider() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static PlayerSpell get(Player player) {
        return player.getData(PLAYER_SPELL);
    }

    private static final class SpellSerializer
            implements IAttachmentSerializer<CompoundTag, PlayerSpell> {
        @Override
        public PlayerSpell read(
                IAttachmentHolder holder,
                CompoundTag tag,
                HolderLookup.Provider registries) {
            PlayerSpell spells = new PlayerSpell();
            spells.loadNBTData(tag);
            return spells;
        }

        @Nullable
        @Override
        public CompoundTag write(PlayerSpell spells, HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            spells.saveNBTData(tag);
            return tag;
        }
    }
}
