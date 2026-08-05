package org.holy.unraveling_spells.client;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.client.buttons.*;
import org.holy.unraveling_spells.client.components.ScrollableTextArea;
import org.holy.unraveling_spells.client.buttons.*;
import org.holy.unraveling_spells.compat.AnimationCompat;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellConflictManager;
import org.holy.unraveling_spells.config.SpellLearnedManager;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.RequestSyncPacket;
import org.holy.unraveling_spells.network.packet.SchoolC2SPacket;
import org.holy.unraveling_spells.network.packet.SpellC2SPacket;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MagicLecternScreen extends AbstractContainerScreen<MagicLecternMenu> {
    public static final ResourceLocation TEXTURE_BG = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/bg.png");
    public static final ResourceLocation TEXTURE_BUTTONS = ResourceLocation.fromNamespaceAndPath("unraveling_spells", "textures/gui/buttons.png");

    MagicLecternTile blockEntity;

    public static final int FONT_COLOR = 0xD9CAD5;
    public static final int FONTDISABLED_COLOR = 0x786D76;

    private int panelWidth, panelHeight, left, top;
    private boolean isSyncing, isSyncingCommonConfig, isSyncingSchools, isSyncingSpells, isInitialized = false;

    private LearningTab activeLearningTab = LearningTab.SCHOOLS;

    //************* SCHOOL *************
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
    private final List<SchoolButtonAnimation> schoolButtonAnimations = new ArrayList<>();
    private final List<Button> schoolControlButtons = new ArrayList<>();
    private int nextSchoolButtonAnimationIndex;
    private long nextSchoolButtonAnimationAt;
    private long schoolListAnimationFinishesAt;
    private boolean schoolListAnimating;

    private static final float SCHOOL_BUTTON_ANIMATION_DURATION = 0.2f;
    private static final long SCHOOL_BUTTON_ANIMATION_DURATION_MS = 200L;
    private static final long SCHOOL_BUTTON_ANIMATION_STAGGER_MS = 100L;
    private static final int SCHOOL_DETAILS_WINDOW_WIDTH = 240;
    private static final int SCHOOL_DETAILS_WINDOW_HEIGHT = 136;
    private static final int SCHOOL_DETAILS_SPELLS_PER_ROW = 10;
    private static final float SCHOOL_DETAILS_ANIMATION_DURATION = 0.2f;
    private static final long SCHOOL_DETAILS_ANIMATION_DURATION_MS = 200L;
    private boolean schoolDetailsWindowOpen;
    private boolean schoolDetailsWindowClosing;
    private float schoolDetailsWindowProgress;
    private long schoolDetailsAnimationEndsAt;
    private int schoolDetailsAnimationId;
    //***************************************

    //************** SPELLS *****************
    private List<AbstractSpell> allSpells = new ArrayList<>();
    private Set<ResourceLocation> learnedSpells = new CopyOnWriteArraySet<>();
    private final List<ResourceLocation> syncedSpellIds = new ArrayList<>();
    private AbstractSpell currentSpell = null;
    private SchoolType currentSchool = null;
    private int currentSpellPage = 0;
    private final List<SpellButton> visibleSpellButtons = new ArrayList<>();
    private ArrowButton previousSpellPageButton;
    private ArrowButton nextSpellPageButton;
    private ArrowButton previousSchoolSwitchButton;
    private ArrowButton nextSchoolSwitchButton;
    private HoldSpellLearnButton learnSpellButton;
    private SchoolType pendingSchool;
    private SpellListTransition spellListTransition = SpellListTransition.NONE;
    private long spellListTransitionEndsAt;
    private float schoolPanelYOffset;
    private float spellTabContentAlpha = 1.0f;
    private ScrollableTextArea spellTextArea;
    private SpellInfoTab activeSpellInfoTab = SpellInfoTab.DESCRIPTION;
    private AbstractSpell spellTextAreaSource;
    private SpellInfoTab spellTextAreaTabSource;
    private boolean spellTextAreaShiftSource;

    private static final int SPELLS_PER_PAGE = 10;
    private static final int SPELL_BUTTON_SIZE = 20;
    private static final int SPELL_LIST_ANIMATION_OFFSET = 10;
    private static final float SPELL_LIST_ANIMATION_DURATION = 0.2f;
    private static final long SPELL_LIST_ANIMATION_DURATION_MS = 250L;
    private static final float SPELL_LIST_FADE_DURATION = 0.20f;
    private static final float SPELL_ARROW_FADE_DURATION = 0.24f;
    private static final float SCHOOL_PANEL_JUMP_HEIGHT = 5.0f;
    private static final float SCHOOL_PANEL_JUMP_DURATION = 0.12f;
    private static final int SPELL_PANEL_WIDTH = 184;
    private static final int SPELL_PANEL_HEIGHT = 108;
    private static final int SPELL_INFO_X_OFFSET = 28;
    private static final int SPELL_INFO_Y_OFFSET = 48;
    private static final String SPELL_CONFLICT_LINK_PREFIX = "unraveling_spells:open_spell/";
    //***************************************

    private static final int QUESTION_WINDOW_WIDTH = 240;
    private static final int QUESTION_WINDOW_HEIGHT = 150;
    private static final float QUESTION_WINDOW_ANIMATION_DURATION = 0.2f;
    private static final long QUESTION_WINDOW_ANIMATION_DURATION_MS = 200L;
    private boolean questionWindowOpen;
    private boolean questionWindowClosing;
    private float questionWindowProgress;
    private long questionWindowAnimationEndsAt;
    private int questionWindowAnimationId;
    private ScrollableTextArea questionTextArea;

    private enum LearningTab {
        SCHOOLS,
        SPELLS
    }

    private enum SpellListTransition {
        NONE,
        EXITING,
        ENTERING
    }

    private enum SpellInfoTab {
        DESCRIPTION,
        CHARACTERISTICS,
        CONFLICTS
    }

    private record SchoolButtonAnimation(Button schoolButton, Button detailsButton,
                                         int targetSchoolX, int targetDetailsX) {
    }

    public MagicLecternScreen(MagicLecternMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        questionWindowOpen = false;
        questionWindowClosing = false;
        questionWindowProgress = 0.0f;
        questionWindowAnimationId++;
        schoolDetailed = null;
        schoolDetailsWindowOpen = false;
        schoolDetailsWindowClosing = false;
        schoolDetailsWindowProgress = 0.0f;
        schoolDetailsAnimationId++;
        spellTabContentAlpha = 1.0f;
        activeSpellInfoTab = SpellInfoTab.DESCRIPTION;
        spellTextAreaSource = null;
        spellTextAreaTabSource = null;
        spellTextAreaShiftSource = false;

        this.inventoryLabelY-= 1000;
        this.blockEntity = getMenu().blockEntity;

        panelWidth = 288;
        panelHeight = 160;

        left = (this.width - 288) / 2;
        top = (this.height - 160) / 2;

        spellTextArea = new ScrollableTextArea(font,
                getSpellInfoX(), getSpellInfoY(),
                getSpellInfoWidth(), getSpellInfoHeight(),
                FONT_COLOR);

        questionTextArea = new ScrollableTextArea(font,
                getQuestionWindowX() + 12,
                getQuestionWindowY() + 32,
                QUESTION_WINDOW_WIDTH - 24,
                QUESTION_WINDOW_HEIGHT - 42,
                FONT_COLOR);
        questionTextArea.setText(Component.translatable("ui.unraveling_spells.question.text"));

        refreshSchoolTypesFromConfig();
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
        updateSchoolListAnimation();
        updateSpellListTransition();
        AnimationCompat.update(this);
        updateSchoolDetailsWindowAnimation();
        updateQuestionWindowAnimation();
        super.render(guiGraphics, p_283661_, p_281248_, p_281886_);

        if (isSyncing) {
            guiGraphics.drawString(font, "Synchronization...",
                    left + (panelWidth - font.width("Synchronization...")) / 2,
                    top + (panelHeight / 2) - 3, FONT_COLOR, true);
        }

        renderSpellLearningFill(guiGraphics);
        renderSpellLearningCostTooltip(guiGraphics, p_283661_, p_281248_);

        if (schoolDetailsWindowOpen) {
            renderSchoolDetailsWindow(guiGraphics, p_283661_, p_281248_);
        }

        if (questionWindowOpen) {
            renderQuestionWindow(guiGraphics, p_283661_, p_281248_);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (schoolDetailsWindowOpen) {
            if (button == 0 && (isSchoolDetailsCloseButtonHovered(mouseX, mouseY)
                    || !isInsideSchoolDetailsWindow(mouseX, mouseY))) {
                closeSchoolDetails();
            }
            return true;
        }
        if (questionWindowOpen) {
            if (button == 0 && (isQuestionCloseButtonHovered(mouseX, mouseY)
                    || !isInsideQuestionWindow(mouseX, mouseY))) {
                closeQuestionWindow();
            }
            if (!questionWindowClosing && questionTextArea != null) {
                questionTextArea.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (isSpellTextAreaAvailable()) {
            if (button == 0 && openClickedConflictSpell(mouseX, mouseY)) {
                return true;
            }
            if (spellTextArea.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (schoolDetailsWindowOpen) {
            return true;
        }
        if (questionWindowOpen) {
            if (questionTextArea != null) questionTextArea.mouseReleased(button);
            return true;
        }
        if (isSpellTextAreaAvailable() && spellTextArea.mouseReleased(button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (schoolDetailsWindowOpen) {
            return true;
        }
        if (questionWindowOpen) {
            if (questionTextArea != null) questionTextArea.mouseScrolled(mouseX, mouseY, delta);
            return true;
        }
        if (isSpellTextAreaAvailable() && spellTextArea.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (schoolDetailsWindowOpen) {
            return true;
        }
        if (questionWindowOpen) {
            if (questionTextArea != null) questionTextArea.mouseDragged(mouseX, mouseY, button);
            return true;
        }
        if (isSpellTextAreaAvailable() && spellTextArea.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (schoolDetailsWindowOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeSchoolDetails();
            }
            return true;
        }
        if (questionWindowOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeQuestionWindow();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        this.renderBackground(guiGraphics);

        guiGraphics.blit(TEXTURE_BG, (this.width - 288) / 2, (this.height - 160) / 2,
                0, 0, 288, 160, 288, 160);

        if (!isSyncing) {
            if (activeLearningTab == LearningTab.SCHOOLS) {
                renderSchoolsTab(guiGraphics, i, i1, v);
            } else {
                renderSpellsTab(guiGraphics, i, i1, v);
            }
        }
    }

    //
    // SCHOOL TAB ********************************************************
    //

    private void learningSchoolsTab() {
        learningSchoolsTab(false);
    }

    private void learningSchoolsTab(boolean allowAnimation) {
        activeLearningTab = LearningTab.SCHOOLS;
        clearWidgets();
        learnSpellButton = null;
        schoolButtonAnimations.clear();
        schoolControlButtons.clear();
        schoolListAnimating = false;

        int visibleSchoolCount = Math.min(SCHOOLS_VISIBLE_COUNT, schoolTypes.size() - currentIndex);
        int lastSchoolButtonX = visibleSchoolCount > 0
                ? getSchoolButtonX(visibleSchoolCount - 1)
                : 0;
        boolean animateSchoolButtons = allowAnimation
                && AnimationCompat.isEnabled()
                && visibleSchoolCount > 0;

        for (int i = 0; i < SCHOOLS_VISIBLE_COUNT; i++) {
            int schoolIndex = currentIndex + i;
            if (schoolIndex >= schoolTypes.size()) break;

            SchoolType school = schoolTypes.get(schoolIndex);
            if (school == null) return;

            final Button[] detailsButtonRef = new Button[1];

            final int BUTTON_X = getSchoolButtonX(i);
            final int BUTTON_START_X = animateSchoolButtons ? lastSchoolButtonX : BUTTON_X;
            final int BUTTON_BASE_Y = top + (panelHeight / 2) - (SCHOOLBUTTON_HEIGHT / 2);
            final int BUTTON_SELECTED_Y = BUTTON_BASE_Y - 5;
            final int BUTTON_Y = selectedSchools.contains(school.getId()) ? BUTTON_SELECTED_Y : BUTTON_BASE_Y;

            SpecialButton schoolButton = new SpecialButton(BUTTON_START_X, BUTTON_Y, SCHOOLBUTTON_WIDTH, SCHOOLBUTTON_HEIGHT) {
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

                    renderSchoolIcon(guiGraphics, school, getX() + 16, getY() + 20);
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

                @Override
                public void onPress() {
                    if (schoolListAnimating) return;

                    ResourceLocation schoolId = school.getId();

                    if (isSchoolContains()) {
                        selectedSchools.remove(schoolId);
                        AnimButtonYTo(this, BUTTON_BASE_Y);
                        AnimButtonYTo(detailsButtonRef[0], BUTTON_BASE_Y + 60);
                    } else {
                        if (selectedSchools.size() < getRequiredSchoolCount()) {
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
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    Button detailsButton = detailsButtonRef[0];
                    if (detailsButton != null && detailsButton.isMouseOver(mouseX, mouseY)) {
                        return false;
                    }
                    return super.mouseClicked(mouseX, mouseY, button);
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
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);

                    ResourceLocation fallbackDetails = ResourceLocation.fromNamespaceAndPath(
                            Unraveling_spells.MODID,
                            "textures/gui/icons/details_en_us.png");
                    ResourceLocation localizedDetails = ResourceLocation.fromNamespaceAndPath(
                            Unraveling_spells.MODID,
                            "textures/gui/icons/details_" + getMinecraft().options.languageCode + ".png");
                    ResourceLocation pathDetails = getMinecraft().getResourceManager()
                            .getResource(localizedDetails)
                            .isPresent()
                            ? localizedDetails
                            : fallbackDetails;

                    if (!isMouseOver(mouseX,mouseY)) guiGraphics.blit(
                            pathDetails,
                            getX(), getY(), 0, 0, 58, 12, 64, 32);
                    if (isMouseOver(mouseX,mouseY)) guiGraphics.blit(
                            pathDetails,
                            getX(), getY(), 0, 16, 58, 12, 64, 32);
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

                @Override
                public boolean isMouseOver(double mouseX, double mouseY) {
                    return mouseX >= getX() && mouseX < getX() + getWidth()
                            && mouseY >= getY() && mouseY < getY() + getHeight();
                }

                @Override
                public void onPress() {
                    openSchoolDetails(school);
                }
            };
            detailsButtonRef[0] = detailsButton;

            if (animateSchoolButtons) {
                schoolButton.setAlpha(0.0f);
                detailsButton.setAlpha(0.0f);
                schoolButtonAnimations.add(new SchoolButtonAnimation(
                        schoolButton,
                        detailsButton,
                        BUTTON_X,
                        BUTTON_X + (SCHOOLBUTTON_WIDTH - 58) / 2));
            }

            addRenderableWidget(schoolButton);
            addRenderableWidget(detailsButton);
        }

        ArrowButton nextButton = new ArrowButton(
                Math.round(left + ((float) panelWidth / 2) + (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2))) - (SCHOOLBUTTON_GAP+9) + 10,
                top + (panelHeight / 2) - 5, "right") {
            @Override
            public void onPress() {
                if (!schoolListAnimating && isActive()) {
                    currentIndex += SCHOOLS_VISIBLE_COUNT;
                    clearWidgets();
                    learningSchoolsTab(!Screen.hasShiftDown());
                }
            }

            @Override
            public boolean isActive() {
                return !schoolListAnimating
                        && currentIndex + SCHOOLS_VISIBLE_COUNT < schoolTypes.size();
            }
        };

        ArrowButton backButton = new ArrowButton(
                Math.round(left + ((float) panelWidth / 2) - (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2))) - 10,
                top + (panelHeight / 2) - 5, "left") {
            @Override
            public void onPress() {
                if (!schoolListAnimating && isActive()) {
                    currentIndex -= SCHOOLS_VISIBLE_COUNT;
                    if (currentIndex < 0) currentIndex = 0;
                    clearWidgets();
                    learningSchoolsTab(!Screen.hasShiftDown());
                }
            }

            @Override
            public boolean isActive() {
                return !schoolListAnimating && currentIndex > 0;
            }
        };

        ClassicButton confirmButton = new HoldConfirmButton(
                left + (panelWidth / 2) - (128/2), top + panelHeight - 30,
                128, 18, Component.translatable(String.format("ui.unraveling_spells.button.need",
                getRequiredSchoolCount() - selectedSchools.size()))) {
            @Override
            public boolean isActive() {
                if (selectedSchools.size() == getRequiredSchoolCount()) {
                    return true;
                }
                return false;
            }

            @Override
            protected boolean canStartHolding() {
                return !schoolListAnimating;
            }

            @Override
            protected void onConfirmed() {
                List<ResourceLocation> selectedSchoolsList = new ArrayList<>(selectedSchools);
                ModMessages.sendToServer(new SchoolC2SPacket(selectedSchoolsList));

                //startSync();
                //isSyncing = true;
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
                        (getRequiredSchoolCount() - selectedSchools.size()));
            }
        };

        addRenderableWidget(nextButton);
        addRenderableWidget(backButton);
        addRenderableWidget(confirmButton);

        renderBookmarks();

        if (animateSchoolButtons) {
            nextButton.setAlpha(0.0f);
            backButton.setAlpha(0.0f);
            confirmButton.setAlpha(0.0f);
            schoolControlButtons.add(nextButton);
            schoolControlButtons.add(backButton);
            schoolControlButtons.add(confirmButton);
            startSchoolListAnimation();
        }
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

    private int getRequiredSchoolCount() {
        return Math.min(Configuration.getMaxSchools(), schoolTypes.size());
    }

    private void refreshSchoolTypesFromConfig() {
        schoolTypes.clear();
        schoolTypes.addAll(SchoolRegistry.REGISTRY.get().getValues());
        if (!Configuration.isEldritchSchoolLearningEnabled()) {
            schoolTypes.remove(SchoolRegistry.ELDRITCH.get());
        }
        schoolTypes.removeIf(school ->
                Configuration.isSchoolLearningDisabled(school.getId()));
    }

    private void renderSchoolIcon(GuiGraphics guiGraphics, SchoolType school, int x, int y) {
        guiGraphics.blit(getSchoolIcon(school),
                x, y,
                0, 0,
                32, 38,
                32, 38);
    }

    //
    // SPELL TAB ********************************************************
    //

    private void learningSpellsTab() {
        activeLearningTab = LearningTab.SPELLS;
        clearWidgets();
        visibleSpellButtons.clear();
        previousSpellPageButton = null;
        nextSpellPageButton = null;
        previousSchoolSwitchButton = null;
        nextSchoolSwitchButton = null;
        learnSpellButton = null;

        List<SchoolType> learnedSchoolTypes = new ArrayList<>(selectedSchools.stream()
                .map(id -> SchoolRegistry.REGISTRY.get().getValue(id))
                .filter(school -> school != null)
                .filter(school -> !Configuration.isSchoolLearningDisabled(school.getId()))
                .filter(school -> Configuration.isEldritchSchoolLearningEnabled()
                        || !SchoolRegistry.ELDRITCH.get().equals(school))
                .toList());

        if (currentSchool != null
                && (Configuration.isEldritchSchoolLearningEnabled()
                || !SchoolRegistry.ELDRITCH.get().equals(currentSchool))
                && learnedSchoolTypes.stream()
                .noneMatch(school -> school.getId().equals(currentSchool.getId()))) {
            learnedSchoolTypes.add(currentSchool);
        }

        if (currentSchool == null) {
            currentSchool = learnedSchoolTypes.stream()
                    .findFirst()
                    .orElse(null);
            currentSpellPage = 0;
        }

        List<AbstractSpell> currentSchoolSpells = getCurrentSchoolSpells();
        if (currentSpell == null || !currentSchoolSpells.contains(currentSpell)) {
            currentSpell = currentSchoolSpells.stream().findFirst().orElse(null);
        }

        if (spellListTransition == SpellListTransition.NONE) {
            spellTabContentAlpha = 1.0f;
        }

        createSpellsButtons();

        int currentSchoolIndex = learnedSchoolTypes.indexOf(currentSchool);
        int schoolPanelX = left + 20;
        int schoolArrowY = top + panelHeight / 2 - 8;

        previousSchoolSwitchButton = new ArrowButton(schoolPanelX - 11, schoolArrowY, "left") {
            @Override
            public void onPress() {
                if (isActive()) {
                    switchSchoolWithSpellListAnimation(learnedSchoolTypes.get(currentSchoolIndex - 1));
                }
            }

            @Override
            public boolean isActive() {
                return active && spellListTransition == SpellListTransition.NONE && currentSchoolIndex > 0;
            }
        };

        nextSchoolSwitchButton = new ArrowButton(schoolPanelX + 56 + 2, schoolArrowY, "right") {
            @Override
            public void onPress() {
                if (isActive()) {
                    switchSchoolWithSpellListAnimation(learnedSchoolTypes.get(currentSchoolIndex + 1));
                }
            }

            @Override
            public boolean isActive() {
                return active && spellListTransition == SpellListTransition.NONE &&
                        currentSchoolIndex >= 0 && currentSchoolIndex + 1 < learnedSchoolTypes.size();
            }
        };

        //mini buttons
        int miniButtonX = getSpellPanelX() + 8;
        int firstMiniButtonY = getSpellPanelY() + 46;
        learnSpellButton = new HoldSpellLearnButton(miniButtonX, firstMiniButtonY) {
            @Override
            protected void onConfirmed() {
                learnCurrentSpell();
            }

            @Override
            public boolean isActive() {
                return active && canLearnCurrentSpell();
            }
        };
        MiniButtons descriptionSpellButton = new MiniButtons(miniButtonX, firstMiniButtonY + 14, MiniButtons.MiniType.DESCRIPTION) {
            @Override
            public void onPress() {
                setSpellInfoTab(SpellInfoTab.DESCRIPTION);
            }

            @Override
            public boolean isActive() {
                return active && currentSpell != null && activeSpellInfoTab != SpellInfoTab.DESCRIPTION;
            }
        };
        MiniButtons conflictsSpellButton = new MiniButtons(miniButtonX, firstMiniButtonY + 28, MiniButtons.MiniType.CONFLICTS) {
            @Override
            public void onPress() {
                setSpellInfoTab(SpellInfoTab.CONFLICTS);
            }

            @Override
            public boolean isActive() {
                return active && currentSpell != null && activeSpellInfoTab != SpellInfoTab.CONFLICTS;
            }
        };
        MiniButtons characteristicSpellButton = new MiniButtons(miniButtonX, firstMiniButtonY + 42, MiniButtons.MiniType.CHARACTERISTIC) {
            @Override
            public void onPress() {
                setSpellInfoTab(SpellInfoTab.CHARACTERISTICS);
            }

            @Override
            public boolean isActive() {
                return active && currentSpell != null && activeSpellInfoTab != SpellInfoTab.CHARACTERISTICS;
            }
        };

        addRenderableWidget(previousSchoolSwitchButton);
        addRenderableWidget(nextSchoolSwitchButton);
        addRenderableWidget(learnSpellButton);
        addRenderableWidget(descriptionSpellButton);
        addRenderableWidget(conflictsSpellButton);
        addRenderableWidget(characteristicSpellButton);

        renderBookmarks();

    }

    private void createSpellsButtons() {
        if (currentSchool == null) return;

        List<AbstractSpell> schoolSpells = getCurrentSchoolSpells();

        int totalPages = Math.max(1, (schoolSpells.size() + SPELLS_PER_PAGE - 1) / SPELLS_PER_PAGE);
        currentSpellPage = Math.max(0, Math.min(currentSpellPage, totalPages - 1));

        int firstSpellIndex = currentSpellPage * SPELLS_PER_PAGE;
        int rowWidth = SPELLS_PER_PAGE * SPELL_BUTTON_SIZE;
        int rowX = left + (panelWidth - rowWidth) / 2;
        int rowY = top + panelHeight - SPELL_BUTTON_SIZE - 7;
        boolean animateEntering = spellListTransition == SpellListTransition.ENTERING;

        for (int positionOnPage = 0; positionOnPage < SPELLS_PER_PAGE; positionOnPage++) {
            int spellIndex = firstSpellIndex + positionOnPage;
            AbstractSpell spell = spellIndex < schoolSpells.size()
                    ? schoolSpells.get(spellIndex)
                    : null;

            SpellButton spellButton = new SpellButton(
                    rowX + positionOnPage * SPELL_BUTTON_SIZE,
                    animateEntering ? rowY + SPELL_LIST_ANIMATION_OFFSET : rowY,
                    spell
            ) {
                @Override
                public void onPress() {
                    if (getSpell() != null) {
                        currentSpell = getSpell();
                    }
                }

                @Override
                public boolean isSelected() {
                    return getSpell() != null && currentSpell == getSpell();
                }

                @Override
                public boolean isBlocked() {
                    return getSpell() != null &&
                            SpellConflictManager.hasConflict(getSpell().getSpellResource(), learnedSpells);
                }

                @Override
                public boolean isLearned() {
                    return getSpell() != null
                            && SpellLearningHelper.isLearned(
                            getSpell(), getMinecraft().player);
                }
            };

            if (animateEntering) {
                spellButton.setAlpha(0.0f);
            }

            visibleSpellButtons.add(spellButton);
            addRenderableWidget(spellButton);

            if (animateEntering) {
                AnimationCompat.animate(this,
                        spellButton.getY(), rowY,
                        SPELL_LIST_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                        value -> spellButton.setY(Math.round(value)));
                AnimationCompat.animate(this,
                        0.0f, 1.0f,
                        SPELL_LIST_FADE_DURATION, AnimationCompat.Easing.LINEAR,
                        spellButton::setAlpha);
            }
        }

        createSpellPageButtons(rowX, rowY, rowWidth, totalPages, animateEntering);
    }

    private void createSpellPageButtons(int rowX, int rowY, int rowWidth, int totalPages, boolean animateFadeIn) {
        previousSpellPageButton = new ArrowButton(rowX - 11, rowY + 2, "left") {
            @Override
            public void onPress() {
                if (spellListTransition == SpellListTransition.NONE && isActive()) {
                    currentSpellPage--;
                    learningSpellsTab();
                }
            }

            @Override
            public boolean isActive() {
                return active && spellListTransition == SpellListTransition.NONE && currentSpellPage > 0;
            }
        };

        nextSpellPageButton = new ArrowButton(rowX + rowWidth + 2, rowY + 2, "right") {
            @Override
            public void onPress() {
                if (spellListTransition == SpellListTransition.NONE && isActive()) {
                    currentSpellPage++;
                    learningSpellsTab();
                }
            }

            @Override
            public boolean isActive() {
                return active && spellListTransition == SpellListTransition.NONE
                        && currentSpellPage + 1 < totalPages;
            }
        };

        if (animateFadeIn) {
            previousSpellPageButton.setAlpha(0.0f);
            nextSpellPageButton.setAlpha(0.0f);
        }

        addRenderableWidget(previousSpellPageButton);
        addRenderableWidget(nextSpellPageButton);

        if (animateFadeIn) {
            AnimationCompat.animate(this,
                    0.0f, 1.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_OUT,
                    previousSpellPageButton::setAlpha);
            AnimationCompat.animate(this,
                    0.0f, 1.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_OUT,
                    nextSpellPageButton::setAlpha);
        }
    }

    private List<AbstractSpell> getCurrentSchoolSpells() {
        if (currentSchool == null) return List.of();

        return allSpells.stream()
                .filter(spell -> spell.getSchoolType() != null)
                .filter(spell -> currentSchool.getId().equals(spell.getSchoolType().getId()))
                .toList();
    }

    //
    // ANIMATIONS *************************************************************
    //

    private int getSchoolButtonX(int positionOnPage) {
        return Math.round(left + ((float) panelWidth / 2)
                - (SCHOOLBUTTON_STEP * ((float) SCHOOLS_VISIBLE_COUNT / 2)))
                + SCHOOLBUTTON_STEP * positionOnPage;
    }

    private void startSchoolListAnimation() {
        if (!AnimationCompat.isEnabled() || schoolButtonAnimations.isEmpty()) {
            finishSchoolListAnimation();
            return;
        }

        schoolListAnimating = true;
        nextSchoolButtonAnimationIndex = 0;
        long now = Util.getMillis();
        nextSchoolButtonAnimationAt = now;
        schoolListAnimationFinishesAt = now
                + (schoolButtonAnimations.size() - 1L) * SCHOOL_BUTTON_ANIMATION_STAGGER_MS
                + SCHOOL_BUTTON_ANIMATION_DURATION_MS;

        for (Button controlButton : schoolControlButtons) {
            AnimationCompat.animate(this,
                    0.0f, 1.0f,
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    controlButton::setAlpha);
        }
    }

    private void updateSchoolListAnimation() {
        if (!schoolListAnimating) return;

        if (!AnimationCompat.isEnabled()) {
            finishSchoolListAnimation();
            return;
        }

        long now = Util.getMillis();
        while (nextSchoolButtonAnimationIndex < schoolButtonAnimations.size()
                && now >= nextSchoolButtonAnimationAt) {
            SchoolButtonAnimation animation = schoolButtonAnimations.get(nextSchoolButtonAnimationIndex);

            AnimationCompat.animate(this,
                    animation.schoolButton().getX(), animation.targetSchoolX(),
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    x -> animation.schoolButton().setX(Math.round(x)));
            AnimationCompat.animate(this,
                    animation.detailsButton().getX(), animation.targetDetailsX(),
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    x -> animation.detailsButton().setX(Math.round(x)));
            AnimationCompat.animate(this,
                    0.0f, 1.0f,
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    animation.schoolButton()::setAlpha);
            AnimationCompat.animate(this,
                    0.0f, 1.0f,
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    animation.detailsButton()::setAlpha);

            nextSchoolButtonAnimationIndex++;
            nextSchoolButtonAnimationAt += SCHOOL_BUTTON_ANIMATION_STAGGER_MS;
        }

        if (nextSchoolButtonAnimationIndex == schoolButtonAnimations.size()
                && now >= schoolListAnimationFinishesAt) {
            finishSchoolListAnimation();
        }
    }

    private void finishSchoolListAnimation() {
        for (SchoolButtonAnimation animation : schoolButtonAnimations) {
            animation.schoolButton().setX(animation.targetSchoolX());
            animation.schoolButton().setAlpha(1.0f);
            animation.detailsButton().setX(animation.targetDetailsX());
            animation.detailsButton().setAlpha(1.0f);
        }
        for (Button controlButton : schoolControlButtons) {
            controlButton.setAlpha(1.0f);
        }
        schoolListAnimating = false;
    }

    private void AnimButtonYTo(Button button, int targetY) {
        if (button == null) return;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            button.setY(targetY);
            return;
        }
        AnimationCompat.animate(this, button.getY(), targetY, 0.35f,
                AnimationCompat.Easing.EASE_OUT, y -> button.setY(Math.round(y)));
    }

    private void openQuestionWindow() {
        questionWindowOpen = true;
        questionWindowClosing = false;
        int animationId = ++questionWindowAnimationId;

        if (questionTextArea != null) {
            questionTextArea.setText(Component.translatable("ui.unraveling_spells.question.text"));
            questionTextArea.resetScroll();
        }

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            questionWindowProgress = 1.0f;
            return;
        }

        questionWindowProgress = 0.0f;
        AnimationCompat.animate(this,
                0.0f, 1.0f,
                QUESTION_WINDOW_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                progress -> {
                    if (animationId == questionWindowAnimationId) {
                        questionWindowProgress = progress;
                    }
                });
    }

    private void closeQuestionWindow() {
        if (!questionWindowOpen || questionWindowClosing) {
            return;
        }

        int animationId = ++questionWindowAnimationId;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            finishClosingQuestionWindow(animationId);
            return;
        }

        questionWindowClosing = true;
        questionWindowAnimationEndsAt = Util.getMillis() + QUESTION_WINDOW_ANIMATION_DURATION_MS;
        AnimationCompat.animate(this,
                questionWindowProgress, 0.0f,
                QUESTION_WINDOW_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                progress -> {
                    if (animationId == questionWindowAnimationId) {
                        questionWindowProgress = progress;
                    }
                });
    }

    private void updateQuestionWindowAnimation() {
        if (questionWindowClosing
                && Util.getMillis() >= questionWindowAnimationEndsAt) {
            finishClosingQuestionWindow(questionWindowAnimationId);
        }
    }

    private void finishClosingQuestionWindow(int animationId) {
        if (animationId != questionWindowAnimationId) {
            return;
        }

        questionWindowProgress = 0.0f;
        questionWindowClosing = false;
        questionWindowOpen = false;
    }

    private void openSchoolDetails(SchoolType school) {
        if (school == null || schoolListAnimating) {
            return;
        }

        schoolDetailed = school;
        schoolDetailsWindowOpen = true;
        schoolDetailsWindowClosing = false;
        questionWindowOpen = false;
        int animationId = ++schoolDetailsAnimationId;

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            schoolDetailsWindowProgress = 1.0f;
            return;
        }

        schoolDetailsWindowProgress = 0.0f;
        AnimationCompat.animate(this,
                0.0f, 1.0f,
                SCHOOL_DETAILS_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                progress -> {
                    if (animationId == schoolDetailsAnimationId) {
                        schoolDetailsWindowProgress = progress;
                    }
                });
    }

    private void closeSchoolDetails() {
        if (!schoolDetailsWindowOpen || schoolDetailsWindowClosing) {
            return;
        }

        int animationId = ++schoolDetailsAnimationId;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            finishClosingSchoolDetails(animationId);
            return;
        }

        schoolDetailsWindowClosing = true;
        schoolDetailsAnimationEndsAt = Util.getMillis() + SCHOOL_DETAILS_ANIMATION_DURATION_MS;
        AnimationCompat.animate(this,
                schoolDetailsWindowProgress, 0.0f,
                SCHOOL_DETAILS_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                progress -> {
                    if (animationId == schoolDetailsAnimationId) {
                        schoolDetailsWindowProgress = progress;
                    }
                });
    }

    private void updateSchoolDetailsWindowAnimation() {
        if (schoolDetailsWindowClosing
                && Util.getMillis() >= schoolDetailsAnimationEndsAt) {
            finishClosingSchoolDetails(schoolDetailsAnimationId);
        }
    }

    private void finishClosingSchoolDetails(int animationId) {
        if (animationId != schoolDetailsAnimationId) {
            return;
        }

        schoolDetailsWindowProgress = 0.0f;
        schoolDetailsWindowClosing = false;
        schoolDetailsWindowOpen = false;
        schoolDetailed = null;
    }

    private void switchSchoolWithSpellListAnimation(SchoolType targetSchool) {
        if (targetSchool == null || targetSchool == currentSchool ||
                spellListTransition != SpellListTransition.NONE) return;

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            currentSchool = targetSchool;
            currentSpellPage = 0;
            currentSpell = null;
            learningSpellsTab();
            return;
        }

        pendingSchool = targetSchool;
        spellListTransition = SpellListTransition.EXITING;
        spellListTransitionEndsAt = Util.getMillis() + SPELL_LIST_ANIMATION_DURATION_MS;
        if (previousSchoolSwitchButton != null) previousSchoolSwitchButton.active = false;
        if (nextSchoolSwitchButton != null) nextSchoolSwitchButton.active = false;
        fadeOutSpellPageButtons();

        AnimationCompat.animate(this,
                schoolPanelYOffset, -SCHOOL_PANEL_JUMP_HEIGHT,
                SCHOOL_PANEL_JUMP_DURATION, AnimationCompat.Easing.EASE_OUT,
                value -> schoolPanelYOffset = value);

        for (SpellButton button : visibleSpellButtons) {
            button.active = false;
            AnimationCompat.animate(this,
                    button.getY(), button.getY() + SPELL_LIST_ANIMATION_OFFSET,
                    SPELL_LIST_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                    value -> button.setY(Math.round(value)));
            AnimationCompat.animate(this,
                    1.0f, 0.0f,
                    SPELL_LIST_FADE_DURATION, AnimationCompat.Easing.LINEAR,
                    button::setAlpha);
        }
    }

    private void fadeOutSpellPageButtons() {
        if (previousSpellPageButton != null) {
            previousSpellPageButton.active = false;
            AnimationCompat.animate(this,
                    1.0f, 0.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_IN,
                    previousSpellPageButton::setAlpha);
        }
        if (nextSpellPageButton != null) {
            nextSpellPageButton.active = false;
            AnimationCompat.animate(this,
                    1.0f, 0.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_IN,
                    nextSpellPageButton::setAlpha);
        }
    }

    private void updateSpellListTransition() {
        if (spellListTransition != SpellListTransition.NONE && !AnimationCompat.isEnabled()) {
            if (pendingSchool != null) {
                currentSchool = pendingSchool;
                pendingSchool = null;
                currentSpellPage = 0;
                currentSpell = null;
            }
            spellListTransition = SpellListTransition.NONE;
            schoolPanelYOffset = 0.0f;
            learningSpellsTab();
            return;
        }

        if (spellListTransition == SpellListTransition.NONE ||
                Util.getMillis() < spellListTransitionEndsAt) return;

        int rowY = top + panelHeight - SPELL_BUTTON_SIZE - 7;

        if (spellListTransition == SpellListTransition.EXITING) {
            for (SpellButton button : visibleSpellButtons) {
                button.setY(rowY + SPELL_LIST_ANIMATION_OFFSET);
                button.setAlpha(0.0f);
            }

            currentSchool = pendingSchool;
            pendingSchool = null;
            currentSpellPage = 0;
            currentSpell = null;
            spellListTransition = SpellListTransition.ENTERING;
            spellListTransitionEndsAt = Util.getMillis() + SPELL_LIST_ANIMATION_DURATION_MS;
            schoolPanelYOffset = -SCHOOL_PANEL_JUMP_HEIGHT;
            learningSpellsTab();
            AnimationCompat.animate(this,
                    -SCHOOL_PANEL_JUMP_HEIGHT, 0.0f,
                    SCHOOL_PANEL_JUMP_DURATION, AnimationCompat.Easing.EASE_OUT,
                    value -> schoolPanelYOffset = value);
            return;
        }

        for (SpellButton button : visibleSpellButtons) {
            button.setY(rowY);
            button.setAlpha(1.0f);
            button.active = true;
        }

        spellListTransition = SpellListTransition.NONE;
        schoolPanelYOffset = 0.0f;
        if (previousSpellPageButton != null) previousSpellPageButton.active = true;
        if (nextSpellPageButton != null) nextSpellPageButton.active = true;
        if (previousSchoolSwitchButton != null) previousSchoolSwitchButton.active = true;
        if (nextSchoolSwitchButton != null) nextSchoolSwitchButton.active = true;
    }

    //
    // TAB RENDER *************************************************************
    //

    private void renderSchoolsTab(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // pass
    }

    private void renderSpellsTab(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, spellTabContentAlpha);

        //school panel
        final int SCHOOL_PANEL_WEIGHT = 56;
        final int SCHOOL_PANEL_HEIGHT = 86;
        final int SCHOOL_PANEL_X = left + 20;
        final int SCHOOL_PANEL_Y = top + (panelHeight/2) - (SCHOOL_PANEL_HEIGHT/2)
                + Math.round(schoolPanelYOffset);

        guiGraphics.blitNineSliced(TEXTURE_BUTTONS, SCHOOL_PANEL_X, SCHOOL_PANEL_Y,
                SCHOOL_PANEL_WEIGHT, SCHOOL_PANEL_HEIGHT,
                10, 10,
                10, 10,
                56, 32,
                0, 48);

        renderSchoolIcon(guiGraphics, currentSchool,
                SCHOOL_PANEL_X + (SCHOOL_PANEL_WEIGHT/2) - 16,
                SCHOOL_PANEL_Y + 10);

        guiGraphics.drawString(font, currentSchool.getDisplayName().getString(),
                SCHOOL_PANEL_X + (SCHOOL_PANEL_WEIGHT - font.width(currentSchool.getDisplayName().getString())) / 2,
                SCHOOL_PANEL_Y + (SCHOOL_PANEL_HEIGHT/2) + 10,
                ((int) (spellTabContentAlpha * 255.0f) << 24) | FONT_COLOR, false);

        if (!selectedSchools.contains(currentSchool.getId())) {
            Component previewOnlyText = Component.translatable("ui.unraveling_spells.school.preview_only");
            int previewTextWidth = SCHOOL_PANEL_WEIGHT - 8;
            int previewTextX = SCHOOL_PANEL_X + 4;
            int previewTextY = SCHOOL_PANEL_Y + 65;
            int previewTextColor = ((int) (spellTabContentAlpha * 255.0f) << 24)
                    | (FONTDISABLED_COLOR & 0xFFFFFF);

            for (FormattedCharSequence line : font.split(previewOnlyText, previewTextWidth)) {
                if (previewTextY + font.lineHeight > SCHOOL_PANEL_Y + SCHOOL_PANEL_HEIGHT - 1) {
                    break;
                }

                guiGraphics.drawString(font, line,
                        previewTextX + (previewTextWidth - font.width(line)) / 2,
                        previewTextY,
                        previewTextColor, false);
                previewTextY += font.lineHeight;
            }
        }

        //spell panel
        final int SPELL_PANEL_X = getSpellPanelX();
        final int SPELL_PANEL_Y = getSpellPanelY();

        guiGraphics.blitNineSliced(TEXTURE_BUTTONS,
                SPELL_PANEL_X, SPELL_PANEL_Y,
                SPELL_PANEL_WIDTH, SPELL_PANEL_HEIGHT,
                10, 10,
                10, 10,
                56, 32,
                0, 48);

        if(currentSpell != null) {
            if (SpellLearningHelper.isLearned(
                    currentSpell, getMinecraft().player)) {
                guiGraphics.blitNineSliced(TEXTURE_BUTTONS,
                        SPELL_PANEL_X+8, SPELL_PANEL_Y+8,
                        36, 36,
                        1, 1,
                        1, 1,
                        20, 20,
                        60, 128);
            } else {
                guiGraphics.blitNineSliced(TEXTURE_BUTTONS,
                        SPELL_PANEL_X+8, SPELL_PANEL_Y+8,
                        36, 36,
                        1, 1,
                        1, 1,
                        20, 20,
                        40, 128);
            }

            guiGraphics.blit(currentSpell.getSpellIconResource(),
                    SPELL_PANEL_X+10, SPELL_PANEL_Y+10, 0, 0, 32, 32, 32, 32);

            renderWrappedSpellTitle(guiGraphics, currentSpell.getDisplayName(getMinecraft().player).setStyle(Style.EMPTY));

            refreshSpellTextArea();
            spellTextArea.render(guiGraphics, mouseX, mouseY);
        }


        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderWrappedSpellTitle(GuiGraphics guiGraphics, Component title) {
        int titleX = getSpellPanelX() + 50;
        int titleY = getSpellPanelY() + 11;
        int titleWidth = SPELL_PANEL_WIDTH - 58;
        int titleBottom = getSpellPanelY() + 43;
        int titleColor = ((int) (spellTabContentAlpha * 255.0f) << 24) | FONT_COLOR;

        for (FormattedCharSequence line : font.split(title, titleWidth)) {
            if (titleY + font.lineHeight > titleBottom) break;
            guiGraphics.drawString(font, line,
                    titleX + (titleWidth - font.width(line)) / 2,
                    titleY,
                    titleColor,
                    false);
            titleY += font.lineHeight + 1;
        }
    }

    private boolean canLearnCurrentSpell() {
        if (currentSpell == null || blockEntity == null
                || spellListTransition != SpellListTransition.NONE) {
            return false;
        }

        ResourceLocation spellId = currentSpell.getSpellResource();
        if (SpellLearningHelper.isSchoolLearningDisabled(currentSpell)) {
            return false;
        }
        boolean eldritch = SpellLearningHelper.isEldritchSpell(currentSpell);
        int learningCost = getCurrentSpellLearningCost();
        boolean hasLearningResource = eldritch
                ? Configuration.isEldritchSchoolLearningEnabled()
                && blockEntity.getStoredEldritchManuscriptCount() >= learningCost
                : blockEntity.getStoredScrollCount() >= learningCost;
        return currentSpell.getSchoolType() != null
                && selectedSchools.contains(currentSpell.getSchoolType().getId())
                && hasLearningResource
                && !learnedSpells.contains(spellId)
                && !SpellLearnedManager.isSpellDefaultLearned(spellId)
                && !SpellConflictManager.hasConflict(spellId, learnedSpells);
    }

    private void learnCurrentSpell() {
        if (!canLearnCurrentSpell()) return;

        ResourceLocation spellId = currentSpell.getSpellResource();
        boolean eldritch = SpellLearningHelper.isEldritchSpell(currentSpell);
        int learningCost = getCurrentSpellLearningCost();
        learnedSpells.add(spellId);
        ModMessages.sendToServer(new SpellC2SPacket(new ArrayList<>(learnedSpells)));
        getMenu().tableSlotChange(spellId, eldritch, learningCost);
        spellTextAreaSource = null;
    }

    private int getCurrentSpellLearningCost() {
        return currentSpell == null
                ? Configuration.getDefaultSpellScrollCost()
                : Configuration.getSpellScrollCost(currentSpell.getSpellResource());
    }

    private void renderSpellLearningCostTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (activeLearningTab != LearningTab.SPELLS
                || currentSpell == null
                || learnSpellButton == null
                || schoolDetailsWindowOpen
                || questionWindowOpen
                || mouseX < learnSpellButton.getX()
                || mouseX >= learnSpellButton.getX() + learnSpellButton.getWidth()
                || mouseY < learnSpellButton.getY()
                || mouseY >= learnSpellButton.getY() + learnSpellButton.getHeight()) {
            return;
        }

        int cost = getCurrentSpellLearningCost();
        Component costText = Component.translatable(
                "ui.unraveling_spells.tooltip.learning_cost", cost);
        ItemStack resource = SpellLearningHelper.isEldritchSpell(currentSpell)
                ? new ItemStack(ItemRegistry.ELDRITCH_PAGE.get())
                : new ItemStack(utsItemRegistry.SPELL_SCROLL.get());
        int tooltipWidth = font.width(costText) + 28;
        int tooltipHeight = 22;
        int tooltipX = Math.max(4, Math.min(mouseX + 4, width - tooltipWidth - 4));
        int tooltipY = Math.max(4, Math.min(mouseY - 8, height - tooltipHeight - 4));

        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 500.0f);
        guiGraphics.fill(
                tooltipX, tooltipY,
                tooltipX + tooltipWidth, tooltipY + tooltipHeight,
                0xF0100010);
        guiGraphics.renderItem(resource, tooltipX + 3, tooltipY + 3);
        guiGraphics.drawString(
                font, costText,
                tooltipX + 23,
                tooltipY + (tooltipHeight - font.lineHeight) / 2,
                0xFFFFFFFF,
                false);
        guiGraphics.flush();
        guiGraphics.pose().popPose();
    }

    private void renderSpellLearningFill(GuiGraphics guiGraphics) {
        if (activeLearningTab != LearningTab.SPELLS
                || currentSpell == null
                || learnSpellButton == null) {
            return;
        }

        float progress = learnSpellButton.getHoldProgress();
        if (progress <= 0.0f) return;

        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 300.0f);

        fillIconFromBottom(guiGraphics,
                getSpellPanelX() + 10,
                getSpellPanelY() + 10,
                32, 32, progress);

        for (SpellButton button : visibleSpellButtons) {
            if (button.getSpell() == currentSpell) {
                fillIconFromBottom(guiGraphics,
                        button.getX() + 2,
                        button.getY() + 2,
                        16, 16, progress);
                break;
            }
        }

        guiGraphics.flush();
        guiGraphics.pose().popPose();
    }

    private void fillIconFromBottom(GuiGraphics guiGraphics, int x, int y,
                                    int width, int height, float progress) {
        int fillHeight = Math.max(1, Math.round(height * Math.min(1.0f, progress)));
        int fillY = y + height - fillHeight;
        guiGraphics.fill(x, fillY, x + width, y + height, 0xA0FFFFFF);
    }

    private void setSpellInfoTab(SpellInfoTab tab) {
        if (tab == null || activeSpellInfoTab == tab) return;
        activeSpellInfoTab = tab;
        spellTextAreaSource = null;
        refreshSpellTextArea();
    }

    private void refreshSpellTextArea() {
        if (spellTextArea == null) return;

        spellTextArea.setBounds(
                getSpellInfoX(), getSpellInfoY(),
                getSpellInfoWidth(), getSpellInfoHeight());

        boolean showLevelChanges = activeSpellInfoTab == SpellInfoTab.CHARACTERISTICS
                && Screen.hasShiftDown();
        if (spellTextAreaSource == currentSpell
                && spellTextAreaTabSource == activeSpellInfoTab
                && spellTextAreaShiftSource == showLevelChanges) {
            return;
        }

        spellTextAreaSource = currentSpell;
        spellTextAreaTabSource = activeSpellInfoTab;
        spellTextAreaShiftSource = showLevelChanges;
        spellTextArea.setText(getCurrentSpellInfoText());
    }

    private Component getCurrentSpellInfoText() {
        if (currentSpell == null) return Component.empty();

        return switch (activeSpellInfoTab) {
            case DESCRIPTION -> Component.translatable(currentSpell.getComponentId() + ".guide");
            case CHARACTERISTICS -> getCurrentSpellCharacteristics();
            case CONFLICTS -> getCurrentSpellConflicts();
        };
    }

    private Component getCurrentSpellCharacteristics() {
        LocalPlayer player = getMinecraft().player;
        if (player == null) {
            return Component.empty();
        }

        int spellLevel = currentSpell.getLevelFor(currentSpell.getMinLevel(), player);
        int nextBaseLevel = Math.min(currentSpell.getMaxLevel(), currentSpell.getMinLevel() + 1);
        int nextSpellLevel = currentSpell.getLevelFor(nextBaseLevel, player);
        boolean showLevelChanges = Screen.hasShiftDown() && nextSpellLevel > spellLevel;
        List<MutableComponent> uniqueInfo = currentSpell.getUniqueInfo(spellLevel, player);
        List<MutableComponent> nextUniqueInfo = currentSpell.getUniqueInfo(nextSpellLevel, player);
        MutableComponent characteristics = Component.empty();

        for (MutableComponent info : uniqueInfo) {
            MutableComponent line = info.copy().withStyle(ChatFormatting.DARK_GREEN);
            if (showLevelChanges) {
                Double currentValue = getTooltipNumericValue(info);
                Double nextValue = getMatchingTooltipNumericValue(info, nextUniqueInfo);
                if (currentValue != null && nextValue != null) {
                    line.append(getLevelChangeComponent(nextValue - currentValue));
                }
            }
            appendCharacteristicLine(characteristics, line);
        }

        if (currentSpell.getCastType() != CastType.INSTANT) {
            MutableComponent castTime = TooltipsUtils.getCastTimeComponent(
                            currentSpell.getCastType(),
                            Utils.timeFromTicks(
                                    currentSpell.getEffectiveCastTime(spellLevel, player),
                                    2))
                    .withStyle(ChatFormatting.BLUE);
            if (showLevelChanges) {
                double currentCastTime =
                        currentSpell.getEffectiveCastTime(spellLevel, player) / 20.0;
                double nextCastTime =
                        currentSpell.getEffectiveCastTime(nextSpellLevel, player) / 20.0;
                castTime.append(getLevelChangeComponent(nextCastTime - currentCastTime));
            }
            appendCharacteristicLine(characteristics, castTime);
        }

        int manaCost = currentSpell.getManaCost(spellLevel);
        if (manaCost > 0) {
            MutableComponent mana = TooltipsUtils.getManaCostComponent(
                            currentSpell.getCastType(), manaCost)
                    .withStyle(ChatFormatting.BLUE);
            if (showLevelChanges) {
                mana.append(getLevelChangeComponent(
                        currentSpell.getManaCost(nextSpellLevel) - manaCost));
            }
            appendCharacteristicLine(characteristics, mana);
        }

        if (currentSpell.getSpellCooldown() > 0) {
            int cooldown = MagicManager.getEffectiveSpellCooldown(
                    currentSpell, player, CastSource.SPELLBOOK);
            appendCharacteristicLine(characteristics,
                    Component.translatable(
                                    "tooltip.irons_spellbooks.cooldown_length_seconds",
                                    Utils.timeFromTicks(cooldown, 2))
                            .withStyle(ChatFormatting.BLUE));
        }

        return characteristics;
    }

    private void appendCharacteristicLine(MutableComponent characteristics, Component line) {
        if (!characteristics.getString().isEmpty()) {
            characteristics.append("\n");
        }
        characteristics.append(line);
    }

    private Double getMatchingTooltipNumericValue(
            MutableComponent currentInfo,
            List<MutableComponent> nextUniqueInfo) {
        if (!(currentInfo.getContents() instanceof TranslatableContents currentContents)) {
            return null;
        }

        for (MutableComponent nextInfo : nextUniqueInfo) {
            if (nextInfo.getContents() instanceof TranslatableContents nextContents
                    && currentContents.getKey().equals(nextContents.getKey())) {
                return getTooltipNumericValue(nextInfo);
            }
        }
        return null;
    }

    private Double getTooltipNumericValue(Component info) {
        if (!(info.getContents() instanceof TranslatableContents contents)
                || contents.getArgs().length == 0) {
            return null;
        }

        Object value = contents.getArgs()[0];
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(value.toString().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Component getLevelChangeComponent(double levelChange) {
        String formattedChange = (levelChange > 0.0001 ? "+" : "") + formatStat(levelChange);
        return Component.translatable("ui.unraveling_spells.spell.level_change", formattedChange);
    }

    private String formatStat(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private Component getCurrentSpellConflicts() {
        List<ResourceLocation> conflictIds = SpellConflictManager.getConflictSpells(currentSpell.getSpellResource());
        if (conflictIds.isEmpty()) {
            return Component.translatable("ui.unraveling_spells.spell.conflicts.none");
        }

        MutableComponent conflicts = Component.translatable("ui.unraveling_spells.spell.conflicts.has");
        for (int index = 0; index < conflictIds.size(); index++) {
            ResourceLocation conflictId = conflictIds.get(index);
            AbstractSpell spell = SpellRegistry.getSpell(conflictId);
            MutableComponent conflictName = spell == null
                    ? Component.literal(conflictId.toString())
                    : spell.getDisplayName(getMinecraft().player).copy().setStyle(Style.EMPTY);

            if (spell != null && spell.getSchoolType() != null) {
                conflictName.withStyle(style -> style
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                SPELL_CONFLICT_LINK_PREFIX + conflictId)));
            }
            conflicts.append(conflictName);

            if (index + 1 < conflictIds.size()) {
                conflicts.append(", ");
            }
        }
        return conflicts;
    }

    private boolean openClickedConflictSpell(double mouseX, double mouseY) {
        if (activeSpellInfoTab != SpellInfoTab.CONFLICTS || spellTextArea == null) {
            return false;
        }

        Style style = spellTextArea.getStyleAtPosition(mouseX, mouseY);
        ClickEvent clickEvent = style == null ? null : style.getClickEvent();
        if (clickEvent == null
                || clickEvent.getAction() != ClickEvent.Action.SUGGEST_COMMAND
                || !clickEvent.getValue().startsWith(SPELL_CONFLICT_LINK_PREFIX)) {
            return false;
        }

        ResourceLocation spellId = ResourceLocation.tryParse(
                clickEvent.getValue().substring(SPELL_CONFLICT_LINK_PREFIX.length()));
        AbstractSpell targetSpell = spellId == null ? null : SpellRegistry.getSpell(spellId);
        if (targetSpell == null || targetSpell.getSchoolType() == null) {
            return false;
        }
        if (SpellLearningHelper.isEldritchSpell(targetSpell)
                && !Configuration.isEldritchSchoolLearningEnabled()) {
            return false;
        }

        List<AbstractSpell> targetSchoolSpells = allSpells.stream()
                .filter(spell -> spell.getSchoolType() != null)
                .filter(spell -> targetSpell.getSchoolType().getId()
                        .equals(spell.getSchoolType().getId()))
                .toList();
        int targetIndex = targetSchoolSpells.indexOf(targetSpell);
        if (targetIndex < 0) return false;

        currentSchool = targetSpell.getSchoolType();
        currentSpell = targetSpell;
        currentSpellPage = targetIndex / SPELLS_PER_PAGE;
        pendingSchool = null;
        spellListTransition = SpellListTransition.NONE;
        schoolPanelYOffset = 0.0f;
        spellTextAreaSource = null;
        learningSpellsTab();
        return true;
    }

    private boolean isSpellTextAreaAvailable() {
        return activeLearningTab == LearningTab.SPELLS
                && !isSyncing
                && currentSpell != null
                && spellTextArea != null;
    }

    private int getSpellPanelX() {
        return left + 91;
    }

    private int getSpellPanelY() {
        return top + 16;
    }

    private int getSpellInfoX() {
        return getSpellPanelX() + SPELL_INFO_X_OFFSET;
    }

    private int getSpellInfoY() {
        return getSpellPanelY() + SPELL_INFO_Y_OFFSET;
    }

    private int getSpellInfoWidth() {
        return SPELL_PANEL_WIDTH - SPELL_INFO_X_OFFSET - 8;
    }

    private int getSpellInfoHeight() {
        return SPELL_PANEL_HEIGHT - SPELL_INFO_Y_OFFSET - 8;
    }

    //
    // MODAL WINDOWS AND BOOKMARKS *******************************************************
    //

    private void renderBookmarks() {
        BookmarkButton scrollsBookmark = new BookmarkButton(left + 5, top + panelHeight - 1, BookmarkButton.BookmarkType.SCROLLS) {
            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
                super.render(guiGraphics, mouseX, mouseY, ticks);

                guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/item/spell_scroll.png"),
                        getX()+1, getY()+1,
                        0, 0,
                        16, 16,
                        16,16);

                if (isMouseOver(mouseX, mouseY)) {
                    guiGraphics.drawString(font, ""+blockEntity.getStoredScrollCount(),
                            getX() + (getWidth() - font.width(""+blockEntity.getStoredScrollCount())) / 2, getY()+18,
                            FONT_COLOR, false);
                }
            }

            @Override
            public boolean isActive() {
                return false;
            }
        };

        addRenderableWidget(scrollsBookmark);

        int nextBookmarkX = left + 25;
        if (Configuration.isEldritchSchoolLearningEnabled()) {
            BookmarkButton eldritchBookmark = new BookmarkButton(
                    nextBookmarkX, top + panelHeight - 1,
                    BookmarkButton.BookmarkType.SCROLLS) {
                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
                    super.render(guiGraphics, mouseX, mouseY, ticks);

                    guiGraphics.renderItem(
                            new ItemStack(ItemRegistry.ELDRITCH_PAGE.get()),
                            getX() + 1, getY() + 1);

                    if (isMouseOver(mouseX, mouseY)) {
                        String count = Integer.toString(
                                blockEntity.getStoredEldritchManuscriptCount());
                        guiGraphics.drawString(font, count,
                                getX() + (getWidth() - font.width(count)) / 2,
                                getY() + 18,
                                FONT_COLOR, false);
                    }
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            addRenderableWidget(eldritchBookmark);
            nextBookmarkX += 20;
        }

        BookmarkButton questionBookmark = new BookmarkButton(nextBookmarkX, top + panelHeight - 1, BookmarkButton.BookmarkType.RED) {
            @Override
            public void onPress() {
                openQuestionWindow();
            }
        };

        addRenderableWidget(questionBookmark);
    }

    private void renderSchoolDetailsWindow(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (schoolDetailed == null) {
            return;
        }

        float progress = Math.max(0.0f, Math.min(1.0f, schoolDetailsWindowProgress));
        float scale = 0.92f + 0.08f * progress;
        int contentAlpha = Math.round(progress * 255.0f);
        int windowX = getSchoolDetailsWindowX();
        int windowY = getSchoolDetailsWindowY();
        float windowCenterX = windowX + SCHOOL_DETAILS_WINDOW_WIDTH / 2.0f;
        float windowCenterY = windowY + SCHOOL_DETAILS_WINDOW_HEIGHT / 2.0f;
        List<AbstractSpell> schoolSpells = allSpells.stream()
                .filter(spell -> spell.getSchoolType() != null)
                .filter(spell -> schoolDetailed.getId().equals(spell.getSchoolType().getId()))
                .toList();
        AbstractSpell hoveredSpell = null;

        // Finish animated school titles before placing the modal above them.
        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 400.0f);
        guiGraphics.fill(0, 0, width, height,
                Math.round(progress * 0xB8) << 24);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(windowCenterX, windowCenterY, 410.0f);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.pose().translate(-windowCenterX, -windowCenterY, 0.0f);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, progress);

        guiGraphics.blitNineSliced(TEXTURE_BUTTONS,
                windowX, windowY,
                SCHOOL_DETAILS_WINDOW_WIDTH, SCHOOL_DETAILS_WINDOW_HEIGHT,
                10, 10,
                10, 10,
                56, 32,
                0, 48);

        Component title = Component.translatable(
                "ui.unraveling_spells.school.spells",
                schoolDetailed.getDisplayName());
        guiGraphics.drawCenteredString(font, title,
                windowX + SCHOOL_DETAILS_WINDOW_WIDTH / 2,
                windowY + 12,
                (contentAlpha << 24) | FONT_COLOR);

        int gridY = windowY + 34;
        for (int index = 0; index < schoolSpells.size(); index++) {
            int row = index / SCHOOL_DETAILS_SPELLS_PER_ROW;
            int positionInRow = index % SCHOOL_DETAILS_SPELLS_PER_ROW;
            int spellsBeforeRow = row * SCHOOL_DETAILS_SPELLS_PER_ROW;
            int spellsInRow = Math.min(
                    SCHOOL_DETAILS_SPELLS_PER_ROW,
                    schoolSpells.size() - spellsBeforeRow);
            int rowX = windowX
                    + (SCHOOL_DETAILS_WINDOW_WIDTH - spellsInRow * SPELL_BUTTON_SIZE) / 2;
            int spellX = rowX + positionInRow * SPELL_BUTTON_SIZE;
            int spellY = gridY + row * SPELL_BUTTON_SIZE;

            if (spellY + SPELL_BUTTON_SIZE > windowY + SCHOOL_DETAILS_WINDOW_HEIGHT - 5) {
                break;
            }

            AbstractSpell spell = schoolSpells.get(index);
            boolean hovered = isScaledSchoolDetailsSpellHovered(
                    mouseX, mouseY,
                    spellX, spellY,
                    scale, windowCenterX, windowCenterY);

            guiGraphics.blit(spell.getSpellIconResource(),
                    spellX + 2, spellY + 2 - (hovered ? 1 : 0),
                    0, 0,
                    16, 16,
                    16, 16);
            if (hovered) {
                guiGraphics.blit(TEXTURE_BUTTONS,
                        spellX, spellY - 1,
                        0, 148,
                        SPELL_BUTTON_SIZE, SPELL_BUTTON_SIZE + 2);
                hoveredSpell = spell;
            } else {
                guiGraphics.blit(TEXTURE_BUTTONS,
                        spellX, spellY,
                        0, 128,
                        SPELL_BUTTON_SIZE, SPELL_BUTTON_SIZE);
            }
        }

        if (schoolSpells.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                    Component.translatable("ui.unraveling_spells.no_spells"),
                    windowX + SCHOOL_DETAILS_WINDOW_WIDTH / 2,
                    windowY + SCHOOL_DETAILS_WINDOW_HEIGHT / 2,
                    (contentAlpha << 24) | FONTDISABLED_COLOR);
        }

        int closeColor = isSchoolDetailsCloseButtonHovered(mouseX, mouseY)
                ? 0xFFFFFF
                : FONTDISABLED_COLOR;
        guiGraphics.drawCenteredString(font, "×",
                windowX + SCHOOL_DETAILS_WINDOW_WIDTH - 12,
                windowY + 9,
                (contentAlpha << 24) | closeColor);

        guiGraphics.flush();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();

        if (hoveredSpell != null && progress >= 0.99f && !schoolDetailsWindowClosing) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 1000.0f);
            guiGraphics.renderTooltip(font,
                    getSchoolDetailsSpellTooltip(hoveredSpell),
                    mouseX, mouseY);
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
    }

    private List<FormattedCharSequence> getSchoolDetailsSpellTooltip(AbstractSpell spell) {
        LocalPlayer player = getMinecraft().player;
        if (player == null) {
            return List.of(Component.translatable(spell.getComponentId()).getVisualOrderText());
        }

        int spellLevel = spell.getLevelFor(spell.getMinLevel(), player);
        List<FormattedCharSequence> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(spell.getComponentId())
                .withStyle(style -> style.withUnderlined(true))
                .getVisualOrderText());

        for (MutableComponent uniqueInfo : spell.getUniqueInfo(spellLevel, player)) {
            tooltip.add(uniqueInfo.copy()
                    .withStyle(ChatFormatting.DARK_GREEN)
                    .getVisualOrderText());
        }

        if (spell.getCastType() != CastType.INSTANT) {
            tooltip.add(TooltipsUtils.getCastTimeComponent(
                            spell.getCastType(),
                            Utils.timeFromTicks(
                                    spell.getEffectiveCastTime(spellLevel, player),
                                    2))
                    .withStyle(ChatFormatting.BLUE)
                    .getVisualOrderText());
        }

        int manaCost = spell.getManaCost(spellLevel);
        if (manaCost > 0) {
            tooltip.add(TooltipsUtils.getManaCostComponent(spell.getCastType(), manaCost)
                    .withStyle(ChatFormatting.BLUE)
                    .getVisualOrderText());
        }

        if (spell.getSpellCooldown() > 0) {
            int cooldown = MagicManager.getEffectiveSpellCooldown(
                    spell, player, CastSource.SPELLBOOK);
            tooltip.add(Component.translatable(
                            "tooltip.irons_spellbooks.cooldown_length_seconds",
                            Utils.timeFromTicks(cooldown, 2))
                    .withStyle(ChatFormatting.BLUE)
                    .getVisualOrderText());
        }

        tooltip.add(Component.empty().getVisualOrderText());
        tooltip.addAll(font.split(
                Component.translatable(spell.getComponentId() + ".guide")
                        .withStyle(ChatFormatting.GRAY),
                180));

        return tooltip;
    }

    private int getSchoolDetailsWindowX() {
        return (width - SCHOOL_DETAILS_WINDOW_WIDTH) / 2;
    }

    private int getSchoolDetailsWindowY() {
        return (height - SCHOOL_DETAILS_WINDOW_HEIGHT) / 2;
    }

    private boolean isInsideSchoolDetailsWindow(double mouseX, double mouseY) {
        int windowX = getSchoolDetailsWindowX();
        int windowY = getSchoolDetailsWindowY();
        return mouseX >= windowX && mouseX < windowX + SCHOOL_DETAILS_WINDOW_WIDTH
                && mouseY >= windowY && mouseY < windowY + SCHOOL_DETAILS_WINDOW_HEIGHT;
    }

    private boolean isSchoolDetailsCloseButtonHovered(double mouseX, double mouseY) {
        int closeX = getSchoolDetailsWindowX() + SCHOOL_DETAILS_WINDOW_WIDTH - 19;
        int closeY = getSchoolDetailsWindowY() + 5;
        return mouseX >= closeX && mouseX < closeX + 14
                && mouseY >= closeY && mouseY < closeY + 14;
    }

    private boolean isScaledSchoolDetailsSpellHovered(double mouseX, double mouseY,
                                                      int spellX, int spellY,
                                                      float scale,
                                                      float centerX, float centerY) {
        float renderedX = centerX + (spellX - centerX) * scale;
        float renderedY = centerY + (spellY - centerY) * scale;
        float renderedSize = SPELL_BUTTON_SIZE * scale;
        return mouseX >= renderedX && mouseX < renderedX + renderedSize
                && mouseY >= renderedY && mouseY < renderedY + renderedSize;
    }

    private void renderQuestionWindow(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        float progress = Math.max(0.0f, Math.min(1.0f, questionWindowProgress));
        float scale = 0.92f + 0.08f * progress;
        int contentAlpha = Math.round(progress * 255.0f);
        int windowX = getQuestionWindowX();
        int windowY = getQuestionWindowY();
        float windowCenterX = windowX + QUESTION_WINDOW_WIDTH / 2.0f;
        float windowCenterY = windowY + QUESTION_WINDOW_HEIGHT / 2.0f;

        // Animated school titles are submitted to the font buffer glyph by glyph.
        // Finish the underlying tab before drawing the modal layer over it.
        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 400.0f);
        guiGraphics.fill(0, 0, width, height,
                Math.round(progress * 0xB8) << 24);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(windowCenterX, windowCenterY, 410.0f);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.pose().translate(-windowCenterX, -windowCenterY, 0.0f);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, progress);
        guiGraphics.blitNineSliced(TEXTURE_BUTTONS,
                windowX, windowY,
                QUESTION_WINDOW_WIDTH, QUESTION_WINDOW_HEIGHT,
                10, 10,
                10, 10,
                56, 32,
                0, 48);

        Component title = Component.translatable("ui.unraveling_spells.question.title");
        guiGraphics.drawCenteredString(font, title,
                windowX + QUESTION_WINDOW_WIDTH / 2,
                windowY + 12,
                (contentAlpha << 24) | FONT_COLOR);

        if (questionTextArea != null) {
            questionTextArea.setBounds(
                    windowX + 12,
                    windowY + 32,
                    QUESTION_WINDOW_WIDTH - 24,
                    QUESTION_WINDOW_HEIGHT - 42);
            questionTextArea.setTextColor((contentAlpha << 24) | FONT_COLOR);
            questionTextArea.render(guiGraphics, mouseX, mouseY);
        }

        int closeColor = isQuestionCloseButtonHovered(mouseX, mouseY)
                ? 0xFFFFFF
                : FONTDISABLED_COLOR;
        guiGraphics.drawCenteredString(font, "×",
                windowX + QUESTION_WINDOW_WIDTH - 12,
                windowY + 9,
                (contentAlpha << 24) | closeColor);

        guiGraphics.flush();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();
    }

    private int getQuestionWindowX() {
        return (width - QUESTION_WINDOW_WIDTH) / 2;
    }

    private int getQuestionWindowY() {
        return (height - QUESTION_WINDOW_HEIGHT) / 2;
    }

    private boolean isInsideQuestionWindow(double mouseX, double mouseY) {
        int windowX = getQuestionWindowX();
        int windowY = getQuestionWindowY();
        return mouseX >= windowX && mouseX < windowX + QUESTION_WINDOW_WIDTH
                && mouseY >= windowY && mouseY < windowY + QUESTION_WINDOW_HEIGHT;
    }

    private boolean isQuestionCloseButtonHovered(double mouseX, double mouseY) {
        int closeX = getQuestionWindowX() + QUESTION_WINDOW_WIDTH - 19;
        int closeY = getQuestionWindowY() + 5;
        return mouseX >= closeX && mouseX < closeX + 14
                && mouseY >= closeY && mouseY < closeY + 14;
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
        isSyncingCommonConfig = true;
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
        refreshSchoolTypesFromConfig();

        selectedSchools.clear();
        selectedSchools.addAll(syncedSchoolIds);
        selectedSchools.removeIf(Configuration::isSchoolLearningDisabled);

        learnedSpells.clear();
        learnedSpells.addAll(syncedSpellIds);

        int requiredSchoolCount = getRequiredSchoolCount();
        if (requiredSchoolCount == 0
                || selectedSchools.size() < requiredSchoolCount) {
            learningSchoolsTab();
        } else {
            learningSpellsTab();
        }
    }

    public void SyncSchools() {
        isSyncingSchools = false;
    }

    public void SyncCommonConfig() {
        isSyncingCommonConfig = false;
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
        if (isSyncingCommonConfig || isSyncingSchools || isSyncingSpells) {
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
