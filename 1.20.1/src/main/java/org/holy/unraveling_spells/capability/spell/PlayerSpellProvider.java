package org.holy.unraveling_spells.capability.spell;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSpellProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerSpell> PLAYER_SPELL = CapabilityManager.get(new CapabilityToken<PlayerSpell>() { });

    PlayerSpell spells = null;
    final LazyOptional<PlayerSpell> optional = LazyOptional.of(this::createPlayerSpell);

    private PlayerSpell createPlayerSpell() {
        if(this.spells == null) {
            this.spells = new PlayerSpell();
        }

        return this.spells;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if(capability == PLAYER_SPELL) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (spells != null) {
            createPlayerSpell().saveNBTData(nbt);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSpell();
        spells.loadNBTData(nbt);
    }
}
