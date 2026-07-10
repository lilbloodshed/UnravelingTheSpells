package org.holy.unraveling_spells.client.screens;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.client.screens.buttons.ArrowButton;
import org.holy.unraveling_spells.client.screens.buttons.ClassicButton;
import org.holy.unraveling_spells.client.screens.buttons.MainButton;
import org.holy.unraveling_spells.client.screens.buttons.SpecialButton;
import org.holy.unraveling_spells.compat.AnimationCompat;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.RequestSyncPacket;
import org.holy.unraveling_spells.network.packet.SchoolC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MagicLecternScreen extends AbstractContainerScreen<MagicLecternMenu> {
    public static final ResourceLocation TEXTURE_BG = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/guitest.png");
    public static final ResourceLocation TEXTURE_BUTTONS = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/buttons.png");

    MagicLecternTile blockEntity;
    public static final int FONT_COLOR = 0xD9CAD5;
    public static final int FONTDISABLED_COLOR = 0x786D76;
    private int panelWidth, panelHeight, left, top;
    private boolean isSyncing, isSyncingSchools, isSyncingSpells, isInitialized = false;

    //************* SCHOOL *************
    private final int MAX_SCHOOLS = Configuration.MAX_SCHOOLS.get();
    private int currentIndex = 0;
    private final int SCHOOLS_VISIBLE_COUNT = 3;
    private List<SchoolType> schoolTypes = new ArrayList<>();
    private Set<ResourceLocation> selectedSchools = new CopyOnWriteArraySet<>();
    private final List<ResourceLocation> syncedSchoolIds = new ArrayList<>();
    private SchoolType schoolDetailed;

    private final int SCHOOLBUTTON_WIDTH = 64;
    private final int SCHOOLBUTTON_HEIGHT = 80;
    private final int SCHOOLBUTTON_GAP = 4;
    private final int SCHOOLBUTTON_STEP = SCHOOLBUTTON_WIDTH + SCHOOLBUTTON_GAP;
    //***************************************

    //************** SPELLS *****************
    private List<AbstractSpell> allSpells = new ArrayList<>();
    private Set<ResourceLocation> learnedSpells = new CopyOnWriteArraySet<>();
    private final List<ResourceLocation> syncedSpellIds = new ArrayList<>();
    private AbstractSpell currentSpell = null;
    //***************************************

    public MagicLecternScreen(MagicLecternMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        this.inventoryLabelY-= 1000;
        this.blockEntity = getMenu().blockEntity;

        panelWidth = 288;
        panelHeight = 160;

        left = (this.width - 288) / 2;
        top = (this.height - 160) / 2;

        schoolTypes.clear();
        schoolTypes.addAll(SchoolRegistry.REGISTRY.get().getValues());
        schoolTypes.remove(SchoolRegistry.ELDRITCH.get());
        allSpells.clear();
        allSpells.addAll(SpellRegistry.getEnabledSpells());

        startSync();
    }

    @Override
    public void removed() {
        AnimationCompat.clear(this);
        super.removed();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int p_283661_, int p_281248_, float p_281886_) {
        AnimationCompat.update(this);
        super.render(guiGraphics, p_283661_, p_281248_, p_281886_);

        if (isSyncing) {
            guiGraphics.drawString(font, "Synchronization...",
                    left + (panelWidth - font.width("Synchronization...")) / 2,
                    top + (panelHeight / 2) - 3, FONT_COLOR, true);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        this.renderBackground(guiGraphics);
        /*
        int backgroundWidth = this.width - BACKGROUND_INSET * 2;
        int backgroundHeight = this.height - BACKGROUND_INSET * 2;
        guiGraphics.blitNineSliced(TEXTURE_BG, BACKGROUND_INSET, BACKGROUND_INSET,
                backgroundWidth, backgroundHeight,
                80, 80,
                80, 80,
                256, 160,
                0, 0);
         */

        guiGraphics.blit(TEXTURE_BG, (this.width - 288) / 2, (this.height - 160) / 2,
                0, 0, 288, 160, 288, 160);
    }

    private void createMainButtons() {
        final int tabHeight = 18;

        MainButton learningTab = new MainButton(left + 15, top+5, (panelWidth / 2) - 15, tabHeight, Component.literal("Learning")) {
            @Override
            public boolean isActive() {
                return false;
            }
        };

        MainButton wipTab = new MainButton((left + (panelWidth / 2)) + 15, top+5, (panelWidth - (panelWidth / 2)) - 30, tabHeight, Component.literal("WIP")) {
            @Override
            public boolean isActive() {
                return false;
            }
        };

        addRenderableWidget(learningTab);
        addRenderableWidget(wipTab);
    }

    //
    // SCHOOL TAB ********************************************************
    //

    private void learningSchoolsTab() {
        clearWidgets();

        for (int i = 0; i < SCHOOLS_VISIBLE_COUNT; i++) {
            int schoolIndex = currentIndex + i;
            if (schoolIndex >= schoolTypes.size()) break;

            SchoolType school = schoolTypes.get(schoolIndex);
            if (school == null) return;

            final Button[] detailsButtonRef = new Button[1];

            final int BUTTON_X = Math.round(left + ((float) panelWidth / 2) - (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2)) + (i * SCHOOLBUTTON_STEP));
            final int BUTTON_BASE_Y = top + (panelHeight / 2) - (SCHOOLBUTTON_HEIGHT / 2);
            final int BUTTON_SELECTED_Y = BUTTON_BASE_Y - 5;
            final int BUTTON_Y = selectedSchools.contains(school.getId()) ? BUTTON_SELECTED_Y : BUTTON_BASE_Y;

            SpecialButton schoolButton = new SpecialButton(BUTTON_X, BUTTON_Y, SCHOOLBUTTON_WIDTH, SCHOOLBUTTON_HEIGHT) {
                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
                    super.render(guiGraphics, mouseX, mouseY, ticks);

                    String displayText = school.getDisplayName().getString();

                    AnimationCompat.TextEffect titleEffect = isSchoolContains()
                            ? AnimationCompat.TextEffect.turbulence(0.3f, 1f)
                            : null;
                    AnimationCompat.drawText(guiGraphics, font, displayText,
                            getX() + (getWidth() - font.width(displayText)) / 2,
                            getY() + 10,
                            getTitleColor(),
                            true,
                            titleEffect);

                    guiGraphics.blit(getSchoolIcon(school), getX() + 16, getY() + 20, 0, 0, 32, 38, 32, 38);
                }

                @Override
                public void onPress() {
                    ResourceLocation schoolId = school.getId();

                    if (isSchoolContains()) {
                        selectedSchools.remove(schoolId);
                        AnimButtonYTo(this, BUTTON_BASE_Y);
                        AnimButtonYTo(detailsButtonRef[0], BUTTON_BASE_Y + 60);
                    } else {
                        if (selectedSchools.size() < Configuration.MAX_SCHOOLS.get()) {
                            selectedSchools.add(schoolId);
                            AnimButtonYTo(this, BUTTON_SELECTED_Y);
                            AnimButtonYTo(detailsButtonRef[0], BUTTON_SELECTED_Y + 60);
                        }
                    }
                }

                @Override
                public boolean isMouseOver(double mouseX, double mouseY) {
                    Button detailsButton = detailsButtonRef[0];
                    if (detailsButton != null && detailsButton.isMouseOver(mouseX, mouseY)) {
                        return false;
                    }
                    return super.isMouseOver(mouseX, mouseY);
                }

                @Override
                public boolean isSchoolContains() {
                    ResourceLocation schoolId = school.getId();
                    return selectedSchools.contains(schoolId);
                }
            };

            Button detailsButton = new Button(new Button.Builder(Component.literal(""), button1 -> {})
                    .pos(schoolButton.getX() + (SCHOOLBUTTON_WIDTH - 58) / 2, schoolButton.getY() + 60)
                    .size(58, 12)) {
                @Override
                protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
                    if (!isMouseOver(mouseX,mouseY)) guiGraphics.blit(
                            ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/icons/details_en.png"),
                            getX(), getY(), 0, 0, 58, 12, 64, 32);
                    if (isMouseOver(mouseX,mouseY)) guiGraphics.blit(
                            ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/icons/details_en.png"),
                            getX(), getY(), 0, 16, 58, 12, 64, 32);
                }

                @Override
                public boolean isMouseOver(double mouseX, double mouseY) {
                    return mouseX >= getX() && mouseX < getX() + getWidth()
                            && mouseY >= getY() && mouseY < getY() + getHeight();
                }

                @Override
                public void onPress() {
                    //openDetailsSchool(school);
                }
            };
            detailsButtonRef[0] = detailsButton;

            addRenderableWidget(schoolButton);
            addRenderableWidget(detailsButton);
        }

        ArrowButton nextButton = new ArrowButton(
                Math.round(left + ((float) panelWidth / 2) + (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2))) - (SCHOOLBUTTON_GAP+9) + 10,
                top + (panelHeight / 2) - 5, "right") {
            @Override
            public void onPress() {
                if (isActive()) {
                    currentIndex += SCHOOLS_VISIBLE_COUNT;
                    clearWidgets();
                    learningSchoolsTab();
                }
            }

            @Override
            public boolean isActive() {
                if (currentIndex + SCHOOLS_VISIBLE_COUNT < schoolTypes.size()) return true;
                return false;
            }
        };

        ArrowButton backButton = new ArrowButton(
                Math.round(left + ((float) panelWidth / 2) - (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2))) - 10,
                top + (panelHeight / 2) - 5, "left") {
            @Override
            public void onPress() {
                if (isActive()) {
                    currentIndex -= SCHOOLS_VISIBLE_COUNT;
                    if (currentIndex < 0) currentIndex = 0;
                    clearWidgets();
                    learningSchoolsTab();
                }
            }

            @Override
            public boolean isActive() {
                if (currentIndex > 0) return true;
                return false;
            }
        };

        ClassicButton confirmButton = new ClassicButton(
                left + (panelWidth / 2) - (128/2), top + panelHeight - 30,
                128, 18, Component.translatable(String.format("ui.unraveling_spells.button.need",
                Configuration.MAX_SCHOOLS.get() - selectedSchools.size()))) {
            @Override
            public boolean isActive() {
                if (selectedSchools.size() == Configuration.MAX_SCHOOLS.get()) {
                    return true;
                }
                return false;
            }

            @Override
            public void onPress() {
                if (isActive()) {
                    List<ResourceLocation> selectedSchoolsList = new ArrayList<>(selectedSchools);
                    ModMessages.sendToServer(new SchoolC2SPacket(selectedSchoolsList));

                    //startSync();
                    //isSyncing = true;
                }
            }

            @Override
            public String getTitle() {
                if (isActive()) {
                    if (Screen.hasShiftDown()) {
                        return Component.translatable("ui.unraveling_spells.button.done").getString();
                    }
                    return Component.translatable("ui.unraveling_spells.button.hold").getString();
                }
                return String.format(Component.translatable("ui.unraveling_spells.button.need").getString(),
                        (MAX_SCHOOLS - selectedSchools.size()));
            }
        };

        addRenderableWidget(nextButton);
        addRenderableWidget(backButton);
        addRenderableWidget(confirmButton);
    }

    private ResourceLocation getSchoolIcon(SchoolType school) {
        if (school == null || school.getId() == null) {
            return ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/schools/null.png");
        }

        ResourceLocation schoolIcon = ResourceLocation.fromNamespaceAndPath(
                Unraveling_spells.MODID,
                String.format("textures/gui/icons/schools/%s_school.png", school.getId().getPath().toLowerCase(Locale.ROOT))
        );
        if (Minecraft.getInstance().getResourceManager().getResource(schoolIcon).isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/schools/null.png");
        }
        return schoolIcon;
    }

    //
    // SPELL TAB ********************************************************
    //

    private void learningSpellsTab() {
        clearWidgets();
    }

    //
    // ANIMATIONS *************************************************************
    //

    private void AnimButtonYTo(Button button, int targetY) {
        if (button == null) return;
        AnimationCompat.animate(this, button.getY(), targetY, 0.35f,
                AnimationCompat.Easing.EASE_OUT, y -> button.setY(Math.round(y)));
    }

    //
    // SYNC ********************************************************
    //
    private void startSync() {
        if (isSyncing) return;

        //Unraveling_spells.LOGGER.info("Sync Started");

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (isInitialized) {
            updateUIFromSyncedData();
            return;
        }

        isSyncing = true;
        isSyncingSchools = true;
        isSyncingSpells = true;
        syncedSchoolIds.clear();
        syncedSpellIds.clear();
        ModMessages.sendToServer(new RequestSyncPacket());
    }

    private synchronized void updateUI(PlayerSchool schoolData, PlayerSpell spellData) {
        syncedSchoolIds.clear();
        syncedSchoolIds.addAll(schoolData.getSchools());
        syncedSpellIds.clear();
        syncedSpellIds.addAll(spellData.getSpells());
        updateUIFromSyncedData();
    }

    private synchronized void updateUIFromSyncedData() {
        selectedSchools.clear();
        selectedSchools.addAll(syncedSchoolIds);

        learnedSpells.clear();
        learnedSpells.addAll(syncedSpellIds);

        if (selectedSchools.size() < Configuration.MAX_SCHOOLS.get()) {
            learningSchoolsTab();
        } else {
            learningSpellsTab();
        }
    }

    public void SyncSchools() {
        isSyncingSchools = false;
    }

    public void SyncSchools(List<ResourceLocation> schools) {
        syncedSchoolIds.clear();
        syncedSchoolIds.addAll(schools);
        SyncSchools();
    }

    public void SyncSpells() {
        isSyncingSpells = false;
    }

    public void SyncSpells(List<ResourceLocation> spells) {
        syncedSpellIds.clear();
        syncedSpellIds.addAll(spells);
        SyncSpells();
    }

    public void onSyncComplete() {
        if (isSyncingSchools || isSyncingSpells) {
            return;
        }

        isSyncing = false;
        isInitialized = true;

        //Unraveling_spells.LOGGER.info("Sync Completed");

        updateUIFromSyncedData();
        if (blockEntity != null) {
            blockEntity.load(blockEntity.saveWithFullMetadata());
        }
    }
}
