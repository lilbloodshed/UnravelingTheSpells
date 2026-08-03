package org.holy.unraveling_spells.capability.school;

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

public class PlayerSchoolProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerSchool> PLAYER_SCHOOL = CapabilityManager.get(new CapabilityToken<PlayerSchool>() { });

    PlayerSchool schools = null;
    final LazyOptional<PlayerSchool> optional = LazyOptional.of(this::createPlayerSchool);

    private PlayerSchool createPlayerSchool() {
        if(this.schools == null) {
            this.schools = new PlayerSchool();
        }

        return this.schools;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if(capability == PLAYER_SCHOOL) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (schools != null) {
            createPlayerSchool().saveNBTData(nbt);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSchool().loadNBTData(nbt);
    }
}
