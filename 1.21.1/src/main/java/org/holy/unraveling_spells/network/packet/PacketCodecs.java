package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class PacketCodecs {
    static final int MAX_KNOWLEDGE_ENTRIES = 4096;

    static final StreamCodec<ByteBuf, List<ResourceLocation>> RESOURCE_LOCATION_LIST =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_KNOWLEDGE_ENTRIES));

    private PacketCodecs() {
    }
}
