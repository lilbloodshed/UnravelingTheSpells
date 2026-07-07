package org.holy.unraveling_spells.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.registries.ItemRegistry;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID)
public class ModEvents {
    private static final String IRONS_SPELLBOOKS_MODID = "irons_spellbooks";
    private static final String IRONS_MERCHANT_TRADES_ADDED = Unraveling_spells.MODID + ".merchant_trades_added";

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerSchool.class);
        event.register(PlayerSpell.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).isPresent()) {
                event.addCapability(new ResourceLocation(Unraveling_spells.MODID, "properties_school"), new PlayerSchoolProvider());
            }
            if (!event.getObject().getCapability(PlayerSpellProvider.PLAYER_SPELL).isPresent()) {
                event.addCapability(new ResourceLocation(Unraveling_spells.MODID, "properties_spell"), new PlayerSpellProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            boolean keepEverything = !event.isWasDeath();
            //Persist summon timers across death
            event.getOriginal().reviveCaps();

            PlayerSchool oldSchoolCap = event.getOriginal().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).orElse(null);
            PlayerSpell oldSpellCap = event.getOriginal().getCapability(PlayerSpellProvider.PLAYER_SPELL).orElse(null);

            PlayerSchool newSchoolCap = event.getEntity().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).orElse(null);
            PlayerSpell newSpellCap = event.getEntity().getCapability(PlayerSpellProvider.PLAYER_SPELL).orElse(null);

            if (newSchoolCap != null && oldSchoolCap != null) {
                newSchoolCap.copyFrom(oldSchoolCap);
            }
            if (newSpellCap != null && oldSpellCap != null) {
                newSpellCap.copyFrom(oldSpellCap);
            }
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getRareTrades().add(new SellItemTrade(ItemRegistry.SPELL_SCROLL.get(), 1, 16, 4, 4, 0.05F));
        event.getRareTrades().add(new SellItemTrade(ItemRegistry.OBLIVION_SCROLL.get(), 1, 38, 1, 8, 0.05F));
    }

    @SubscribeEvent
    public static void onIronMerchantInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

        if (entityId == null || !IRONS_SPELLBOOKS_MODID.equals(entityId.getNamespace()) || !(target instanceof Merchant merchant)) {
            return;
        }

        if (target.getPersistentData().getBoolean(IRONS_MERCHANT_TRADES_ADDED)) {
            return;
        }

        MerchantOffers offers = merchant.getOffers();
        addOfferIfMissing(offers, ItemRegistry.SPELL_SCROLL.get(), 1, 16, 4, 4, 0.05F);
        addOfferIfMissing(offers, ItemRegistry.OBLIVION_SCROLL.get(), 1, 38, 1, 8, 0.05F);
        target.getPersistentData().putBoolean(IRONS_MERCHANT_TRADES_ADDED, true);
    }

    private static void addOfferIfMissing(MerchantOffers offers, Item item, int count, int emeraldCost, int maxUses, int xp, float priceMultiplier) {
        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(item)) {
                return;
            }
        }

        offers.add(createSellOffer(item, count, emeraldCost, maxUses, xp, priceMultiplier));
    }

    private static MerchantOffer createSellOffer(Item item, int count, int emeraldCost, int maxUses, int xp, float priceMultiplier) {
        return new MerchantOffer(
                new ItemStack(Items.EMERALD, emeraldCost),
                ItemStack.EMPTY,
                new ItemStack(item, count),
                maxUses,
                xp,
                priceMultiplier
        );
    }

    private static class SellItemTrade implements VillagerTrades.ItemListing {
        private final Item item;
        private final int count;
        private final int emeraldCost;
        private final int maxUses;
        private final int xp;
        private final float priceMultiplier;

        private SellItemTrade(Item item, int count, int emeraldCost, int maxUses, int xp, float priceMultiplier) {
            this.item = item;
            this.count = count;
            this.emeraldCost = emeraldCost;
            this.maxUses = maxUses;
            this.xp = xp;
            this.priceMultiplier = priceMultiplier;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, net.minecraft.util.RandomSource random) {
            return createSellOffer(item, count, emeraldCost, maxUses, xp, priceMultiplier);
        }
    }
}
