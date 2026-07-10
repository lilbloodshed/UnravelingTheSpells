    package org.holy.unraveling_spells.client.screens;

    import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
    import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
    import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
    import io.redspace.ironsspellbooks.api.spells.SchoolType;
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.gui.Font;
    import net.minecraft.client.gui.GuiGraphics;
    import net.minecraft.client.gui.components.Button;
    import net.minecraft.client.gui.components.events.GuiEventListener;
    import net.minecraft.client.gui.screens.Screen;
    import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
    import net.minecraft.client.player.LocalPlayer;
    import net.minecraft.network.chat.Component;
    import net.minecraft.network.chat.Style;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraft.world.entity.player.Inventory;
    import net.minecraft.world.item.ItemStack;
    import org.holy.unraveling_spells.client.buttons_old.magic_screen.*;
    import org.holy.unraveling_spells.config.Configuration;
    import org.holy.unraveling_spells.Unraveling_spells;
    import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;
    import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
    import org.holy.unraveling_spells.capability.school.PlayerSchool;
    import org.holy.unraveling_spells.capability.spell.PlayerSpell;
    import org.holy.unraveling_spells.client.buttons.magic_screen.*;
    import org.holy.unraveling_spells.compat.AnimationCompat;
    import org.holy.unraveling_spells.config.SpellConflictManager;
    import org.holy.unraveling_spells.config.SpellLearnedManager;
    import org.holy.unraveling_spells.network.ModMessages;
    import org.holy.unraveling_spells.network.packet.RequestSyncPacket;
    import org.holy.unraveling_spells.network.packet.SchoolC2SPacket;
    import org.holy.unraveling_spells.network.packet.SpellC2SPacket;

    import java.util.*;
    import java.util.concurrent.CopyOnWriteArraySet;

    public class LearningScreenOld extends AbstractContainerScreen<MagicLecternMenu> {
        public static final ResourceLocation MAIN =
                ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/magic_table_gui.png");

        public static final ResourceLocation MAIN_INV =
                ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/magic_table_inv.png");

        public static final ResourceLocation ICONS =
                ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/small_icons.png");

        private final int FONT_COLOR = 0xD9CAD5;

        private int left, top, page = 0;

        private boolean isSyncingSchool, isSyncingSpell, isSyncing, isInitialized = false;
        private boolean isRenderingWidgets = false;
        private boolean pendingSpellUiUpdate = false;
        private boolean pendingLearningSpellListUpdate = false;
        private boolean pendingLearnSchoolListUpdate = false;

        private MagicLecternTile blockEntity;

        //************* SPELLS *************
        private int currentSpellPage, currentSpellSmallInfo, totalSpellPages = 0;
        private SchoolType currentSchool;
        private List<AbstractSpell> allSpells = new ArrayList<>();
        private Set<ResourceLocation> learnedSpells = new CopyOnWriteArraySet<>();
        private AbstractSpell currentSpell = null;
        private int currentSpellLvl = 1;
        private int currentSpellLvlY;
        private boolean isLearnButtonBlocked = true;
        private int SPELLS_COLS = 1;
        private int titleY = top + 30;

        private boolean isButtonPressed = false;
        private float fillProgress = 0f;
        private long pressStartTime, lastSoundTime = 0L;
        private static final float FILL_DURATION = 3000f;
        private boolean spellLearned, finalSoundPlayed = false;
        private int currentScale = 0;
        private static final int TOTAL_SCALES = 3;

        // Scrollbar
        private int scrollOffset = 0;
        private int SCROLL_STEP, SCROLLBAR_HEIGHT;
        private final int SCROLLBAR_WIDTH = 3;
        private final int SCROLLBAR_X = 245;
        private final int SCROLLBAR_Y = 75;
        private boolean isScrolling = false;
        private int scrollbarDragStartY = 0;
        private int scrollOffsetDragStart = 0;
        //***************************************

        //************* SCHOOL *************
        private int currentIndex = 0;
        private final int maxSchools = Configuration.MAX_SCHOOLS.get();
        private List<SchoolType> schoolTypes = new ArrayList<>();
        private final int VISIBLE_COUNT = 3;
        private Set<ResourceLocation> selectedSchools = new CopyOnWriteArraySet<>();
        private final List<ResourceLocation> syncedSchoolIds = new ArrayList<>();
        private final List<ResourceLocation> syncedSpellIds = new ArrayList<>();
        private SchoolType detailsSchool;
        //***************************************

        public LearningScreenOld(MagicLecternMenu menu, Inventory playerInventory, Component title) {
            super(menu, playerInventory, title);
        }

        public int getPage() {
            return this.page;
        }

        @Override
        protected void init() {
            super.init();
            clearWidgets();

            left = (this.width - 256) / 2;
            top  = (this.height - 160) / 2;

            currentSpellLvlY = top + 62;

            SCROLL_STEP = font.lineHeight;
            SCROLLBAR_HEIGHT = font.lineHeight * 6;

            schoolTypes.clear();
            schoolTypes.addAll(SchoolRegistry.REGISTRY.get().getValues());
            schoolTypes.remove(SchoolRegistry.ELDRITCH.get());

            allSpells.clear();
            allSpells.addAll(SpellRegistry.getEnabledSpells());

            isInitialized = false;
            openList();

            this.blockEntity = getMenu().blockEntity;
            this.inventoryLabelY-= 1000;
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
            AnimationCompat.update(this);
            this.renderBackground(guiGraphics);

            guiGraphics.blit(MAIN, left, top, 0, 0, 256, 160);

            //inv and hotbar
            //guiGraphics.blit(MAIN_INV, left-79, top-4, 0, 0, 74, 168);
            //guiGraphics.blit(MAIN_INV, left + (256 - 192) / 2, top+165, 0, 192, 192, 26);

            if (isSyncing) {
                guiGraphics.drawString(font, "Synchronization",
                        left + (256 - font.width("Synchronization")) / 2,
                        top + 15, FONT_COLOR, true);
            }

            /*
             * page 1 - choosing a magic school
             * page 2 - spells learning
             * page 3 - detailed school - new
             */
            if (page == 1) {
                Component canLearn = Component.translatable("ui.unraveling_spells.school_choose");
                guiGraphics.drawString(font, canLearn,
                        left + (256 - font.width(canLearn)) / 2,
                        top + 20, FONT_COLOR, true);

                Component hasLearn = Component.translatable("ui.unraveling_spells.school_choose2")
                        .append(" " + (maxSchools - selectedSchools.size()));
                guiGraphics.drawString(font, hasLearn,
                        left + (256 - font.width(hasLearn)) / 2,
                        top + 34, FONT_COLOR, true);
            }
            else if (page == 2) {
                guiGraphics.fill(left + ((SPELLS_COLS == 2) ? 76 : 50), top + 20,
                        left+ ((SPELLS_COLS == 2) ? 77 : 51), top+130, 0xffb5a383);
                guiGraphics.blit(MAIN_INV, left+55, top+139, 0, 240, 16, 16);

                if (currentSchool != null) {
                    String schoolTitle = currentSchool.getDisplayName().getString();
                    guiGraphics.drawString(
                            font,
                            schoolTitle,
                            left + (256 - font.width(schoolTitle)) / 2,
                            top + 143,
                            FONT_COLOR
                    );
                }

                if (currentSpell != null) {
                    Component spellTitle = currentSpell.getDisplayName(getMinecraft().player);
                    Component spellSchool = currentSpell.getSchoolType().getDisplayName();

                    // temporary stub if the name doesn't fit
                    List<Component> wrappedSpell = wrapTextToLines(spellTitle.getString(), font, (SPELLS_COLS == 2) ? 120 : 150, Style.EMPTY); // max width 120 p.
                    titleY = top + 30;

                    for (Component line : wrappedSpell) {
                        guiGraphics.drawString(
                                font,
                                line,
                                (left + (256 - font.width(line)) / 2) + ((SPELLS_COLS == 2) ? 60 : 40),
                                titleY,
                                FONT_COLOR
                        );
                        titleY += font.lineHeight + 2;
                    }

                    List<Component> wrappedSchoolSpell = wrapTextToLines(spellSchool.getString(), font, (SPELLS_COLS == 2) ? 120 : 150);
                    for (Component line : wrappedSchoolSpell) {
                        guiGraphics.drawString(
                                font,
                                spellSchool,
                                (left + (256 - font.width(line)) / 2) + ((SPELLS_COLS == 2) ? 60 : 40),
                                titleY,
                                FONT_COLOR,
                                false
                        );
                        titleY += font.lineHeight + 2;
                    }

                    guiGraphics.drawString(
                            font,
                            Component.translatable("ui.unraveling_spells.spell.level").getString() + " " + currentSpellLvl,
                            (left + (256 - font.width(Component.translatable("ui.unraveling_spells.spell.level").getString() + " " + currentSpellLvl)) / 2)
                                    + ((SPELLS_COLS == 2) ? 60 : 40),
                            currentSpellLvlY,
                            FONT_COLOR,
                            false
                    );

                    if (isButtonPressed) {
                        long currentTime = System.currentTimeMillis();
                        long pressDuration = currentTime - pressStartTime;

                        if (pressDuration < (long) FILL_DURATION) {
                            int scale = (int) (pressDuration / 1000L);

                            if (scale < TOTAL_SCALES && scale != currentScale) {
                                currentScale = scale;
                                fillProgress = (float) (currentScale + 1) / TOTAL_SCALES; // 1/3, 2/3, 3/3

                                playFillingSound(fillProgress);
                                lastSoundTime = currentTime;
                            }
                        } else if (!finalSoundPlayed) {
                            playFinalSound();
                            finalSoundPlayed = true;

                            learnSpell();
                            resetProgress();
                            spellLearned = true;
                        }
                    }

                    // spell's icon
                    guiGraphics.blit(currentSpell.getSpellIconResource(),
                            left + ((SPELLS_COLS == 2) ? 90 : 60),
                            top + 24, 0, 0, 32, 32, 32, 32);
                    guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "textures/gui/icons/border.png"),
                            left + ((SPELLS_COLS == 2) ? 90 : 60) - 3,
                            top + 24 - 3, 0, 0, 38, 38, 38, 38);

                    if (isButtonPressed && fillProgress > 0f) {
                        renderFillAnimation(guiGraphics, mouseX, mouseY);
                    }

                    switch (currentSpellSmallInfo) {
                        case 0:
                            renderCurrentSpellInfo(guiGraphics, mouseX, mouseY);
                            break;
                        case 1:
                            renderCurrentSpellConflicts(guiGraphics, mouseX, mouseY);
                            break;
                    }
                }

                String pageInfo = String.format("%d/%d", currentSpellPage + 1, totalSpellPages);
                guiGraphics.drawString(font, pageInfo,
                        left + (11*SPELLS_COLS), top + 132, 0xD9CAD5, true);
            } else if (page == 3) {
                // school icon
                guiGraphics.blit(MAIN, left + 12, top + 35, 192, 160, 56, 96);

                guiGraphics.blit(getSchoolIcon(detailsSchool), left + 19, top + 50, 0, 0, 42, 48, 42, 48);

                guiGraphics.drawString(font, detailsSchool.getDisplayName().getString(), left + 12 + (56 - font.width(detailsSchool.getDisplayName().getString())) / 2,
                        top + 110, 0xD9CAD5, false);

                // separators
                guiGraphics.fill(left + 70, top + 10,
                        left + 71, top + 150, 0xffb5a383);
                guiGraphics.fill(left + 100, top + 30,
                        left + 220, top + 31, 0xffb5a383);
            }

            removeNullRenderables();
            isRenderingWidgets = true;
            try {
                super.render(guiGraphics, mouseX, mouseY, ticks);
            } finally {
                isRenderingWidgets = false;
            }
            runPendingWidgetUpdates();
        }

        private void removeNullRenderables() {
            this.renderables.removeIf(Objects::isNull);
        }

        private void runPendingWidgetUpdates() {
            if (pendingLearningSpellListUpdate) {
                pendingLearningSpellListUpdate = false;
                pendingLearnSchoolListUpdate = false;
                pendingSpellUiUpdate = false;
                openLearningSpellList();
                return;
            }

            if (pendingLearnSchoolListUpdate) {
                pendingLearnSchoolListUpdate = false;
                pendingSpellUiUpdate = false;
                openLearnSchoolList();
                return;
            }

            if (pendingSpellUiUpdate) {
                pendingSpellUiUpdate = false;
                updateSpellUI();
            }
        }

        private void resetProgress() {
            fillProgress = 0f;
            currentScale = 0;
            isButtonPressed = false;
            pressStartTime = 0L;
            spellLearned = false;
            finalSoundPlayed = false;
            lastSoundTime = 0L;
        }

        private void learnSpell() {
            if (currentSpell == null || isLearnButtonBlocked || learnedSpells.contains(currentSpell.getSpellResource())) {
                return;
            }

            if (!isLearnButtonBlocked || learnedSpells.contains(currentSpell.getSpellResource())) {
                if (getMenu().getTableSlotItem().getCount() > 0) {
                    learnedSpells.add(currentSpell.getSpellResource());
                    List<ResourceLocation> selectedSpellsList = new ArrayList<>(learnedSpells);

                    ModMessages.sendToServer(new SpellC2SPacket(selectedSpellsList));
                    ModMessages.sendToServer(new RequestSyncPacket());

                    getMenu().tableSlotChange();
                    updateSpellUI();
                }
            }
        }


        private void playFillingSound(float progress) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.playSound(
                        Unraveling_spells.SPELL_FILL.get(),
                        0.8f,
                        0.9f + (progress * 0.2f)
                );
            }
        }

        private void playFinalSound() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.playSound(
                        Unraveling_spells.SPELL_LEARN.get(),
                        1.0f,
                        1.0f
                );
            }
        }

        private void renderFillAnimation(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            int iconX = left + ((SPELLS_COLS == 2) ? 90 : 60);
            int iconY = top + 24;
            int iconWidth = 32;
            int iconHeight = 32;

            int scaleHeight = iconHeight / TOTAL_SCALES;
            int fillHeight = currentScale * scaleHeight;
            int fillY = iconY + iconHeight - fillHeight;

            int color = (160 << 24) | 0xFFFFFF;

            guiGraphics.fill(iconX, fillY, iconX + iconWidth, iconY + iconHeight, color);
        }

        private void renderCurrentSpellInfo(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            List<Component> spellInfo = new ArrayList<>(getSpellInfo());

            final int spellInfoY = top + 75;
            for (int i = 0; i < spellInfo.size(); i++) {
                Component line = spellInfo.get(i);
                int lineY = spellInfoY + i * font.lineHeight - scrollOffset;

                if (lineY >= top + 75 && lineY <= top + 75 + SCROLLBAR_HEIGHT) {
                    guiGraphics.drawString(
                            font,
                            line,
                            left + ((SPELLS_COLS == 2) ? 86 : 56),
                            lineY,
                            FONT_COLOR
                    );
                }
            }

            renderScrollbar(guiGraphics, mouseX, mouseY);
        }

        private void renderCurrentSpellConflicts(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            List<ResourceLocation> conflicts = new ArrayList<>(SpellConflictManager.getConflictSpells(currentSpell.getSpellResource()));

            String conflicting = Component.translatable("ui.unraveling_spells.spell.conflicts.none").getString();
            if (!conflicts.isEmpty()) {
                conflicting = Component.translatable("ui.unraveling_spells.spell.conflicts.has").getString();
                for (ResourceLocation id : conflicts) {
                    if (conflicts.indexOf(id) + 1 == conflicts.size()) {
                        conflicting += SpellRegistry.getSpell(id).getSpellName();
                    } else {
                        conflicting += SpellRegistry.getSpell(id).getSpellName() + ", ";
                    }
                }
            }

            List<Component> lines = new ArrayList<>(wrapTextToLines(conflicting, font, (SPELLS_COLS == 2) ? 150 : 180));

            final int spellInfoY = top + 75;
            for (int i = 0; i < lines.size(); i++) {
                Component line = lines.get(i);
                int lineY = spellInfoY + i * font.lineHeight - scrollOffset;

                if (lineY >= top + 75 && lineY <= top + 75 + SCROLLBAR_HEIGHT) {
                    guiGraphics.drawString(
                            font,
                            line,
                            left + ((SPELLS_COLS == 2) ? 86 : 56),
                            lineY,
                            FONT_COLOR
                    );
                }
            }

            renderScrollbar(guiGraphics, mouseX, mouseY);
        }

        @Override
        protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

        }

        private List<Component> getSpellInfo() {
            List<Component> lines = new ArrayList<>();

            String castType = currentSpell.getCastType().name().toLowerCase();
            String manaCost = String.valueOf(currentSpell.getManaCost(currentSpellLvl));
            String spellCooldown = String.valueOf(currentSpell.getSpellCooldown() / 20);
            String castTime = String.valueOf(currentSpell.getCastTime(currentSpellLvl) / 20);
            Component description = Component.translatable(String.format("%s.guide", currentSpell.getComponentId()));

            lines.add(Component.translatable("ui.unraveling_spells.spell.cast_type")
                    .append(Component.translatable("spell.unraveling_spells.cast_type." + castType)
                            .getString()));
            lines.add(Component.translatable("ui.unraveling_spells.spell.cast_time").append(castTime.equals("0") ? "Instant" : castTime));
            lines.add(Component.translatable("ui.unraveling_spells.spell.mana_cost").append(manaCost));
            lines.add(Component.translatable("ui.unraveling_spells.spell.cooldown").append(spellCooldown));
            //if (spellPower > 0f) lines.add(Component.translatable("ui.unraveling_spells.spell.spellPower").append("" + spellPower));

            lines.add(Component.literal(" "));
            lines.addAll(wrapTextToLines(description.getString(), font, (SPELLS_COLS == 2) ? 150 : 180, Style.EMPTY));

            return lines;
        }

        private List<Component> wrapTextToLines(String text, Font font, int maxWidth) {
            return wrapTextToLines(text, font, maxWidth, Style.EMPTY);
        }

        private List<Component> wrapTextToLines(String text, Font font, int maxWidth, Style style) {
            List<Component> lines = new ArrayList<>();
            if (text.isEmpty()) {
                lines.add(Component.literal("").withStyle(style));
                return lines;
            }

            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                int currentWidth = font.width(currentLine.toString());
                int wordWidth = font.width(word);

                if (currentWidth + wordWidth <= maxWidth || currentLine.isEmpty()) {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(Component.literal(currentLine.toString()).withStyle(style));
                    }

                    if (wordWidth > maxWidth) {
                        while (!word.isEmpty()) {
                            int charsToTake = 1;
                            while (charsToTake < word.length() &&
                                    font.width(word.substring(0, charsToTake)) <= maxWidth) {
                                charsToTake++;
                            }
                            if (charsToTake > 1) charsToTake--;

                            lines.add(Component.literal(word.substring(0, charsToTake)).withStyle(style));
                            word = word.substring(charsToTake);
                        }
                    } else {
                        currentLine = new StringBuilder(word);
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(Component.literal(currentLine.toString()).withStyle(style));
            }

            return lines;
        }

        //SCROLLBAR
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (page == 2 && currentSpell != null) {
                int thumbHeight = Math.max(10, (int)(SCROLLBAR_HEIGHT * SCROLLBAR_HEIGHT / (getMaxScrollOffset() + SCROLLBAR_HEIGHT)));
                float scrollRatio = (float) scrollOffset / getMaxScrollOffset();
                int thumbY = top + SCROLLBAR_Y + (int)((SCROLLBAR_HEIGHT - thumbHeight) * scrollRatio);

                if (mouseX >= left + SCROLLBAR_X && mouseX <= left + SCROLLBAR_X + SCROLLBAR_WIDTH &&
                        mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    isScrolling = true;
                    scrollbarDragStartY = (int) mouseY;
                    scrollOffsetDragStart = scrollOffset;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            isScrolling = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (isScrolling) {
                int deltaY = (int)(mouseY - scrollbarDragStartY);
                scrollOffset = Math.max(0, Math.min(
                        getMaxScrollOffset(),
                        scrollOffsetDragStart + deltaY * 2 // smooth scrolling
                ));
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
            if (page == 2 && currentSpell != null &&
                    mouseX >= left + ((SPELLS_COLS == 2) ? 87 : 57) && mouseX <= left + 237 &&
                    mouseY >= top + 75 && mouseY <= top + 75 + SCROLLBAR_HEIGHT) {

                scrollOffset += (int)(-deltaY * SCROLL_STEP);
                scrollOffset = Math.max(0, Math.min(getMaxScrollOffset(), scrollOffset));
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, deltaY);
        }

        private int getMaxScrollOffset() {
            List<Component> spellInfo = getSpellInfo();
            int totalHeight = spellInfo.size() * font.lineHeight;
            int visibleHeight = SCROLLBAR_HEIGHT;
            return Math.max(0, totalHeight - visibleHeight);
        }

        private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            guiGraphics.fill(
                    left + SCROLLBAR_X, top + SCROLLBAR_Y,
                    left + SCROLLBAR_X + SCROLLBAR_WIDTH, top + SCROLLBAR_Y + SCROLLBAR_HEIGHT,
                    0xFF404040
            );

            int thumbHeight = Math.max(10, (int)(SCROLLBAR_HEIGHT * SCROLLBAR_HEIGHT / (getMaxScrollOffset() + SCROLLBAR_HEIGHT)));
            float scrollRatio = (float) scrollOffset / getMaxScrollOffset();
            int thumbY = top + SCROLLBAR_Y + (int)((SCROLLBAR_HEIGHT - thumbHeight) * scrollRatio);

            guiGraphics.fill(
                    left + SCROLLBAR_X, thumbY,
                    left + SCROLLBAR_X + SCROLLBAR_WIDTH, thumbY + thumbHeight,
                    0xFF808080
            );

            if (mouseX >= left + SCROLLBAR_X && mouseX <= left + SCROLLBAR_X + SCROLLBAR_WIDTH &&
                    mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                guiGraphics.fill(
                        left + SCROLLBAR_X, thumbY,
                        left + SCROLLBAR_X + SCROLLBAR_WIDTH, thumbY + thumbHeight,
                        0xFFA0A0A0
                );
            }
        }


        // page 2 rendering
        private void openLearningSpellList() {
            if (isRenderingWidgets) {
                pendingLearningSpellListUpdate = true;
                return;
            }

            removeAllWidgets(); // clean the widgets

            List<ResourceLocation> selectedList = new ArrayList<>(selectedSchools);

            if (currentIndex < 0 || currentIndex >= selectedList.size()) {
                currentIndex = 0;
            }

            if (!selectedSchools.isEmpty()) {
                ResourceLocation schoolId = selectedList.get(currentIndex);
                currentSchool = SchoolRegistry.getSchool(schoolId);
            } else {
                Unraveling_spells.LOGGER.error("Selected schools of player is empty");
                currentSchool = null;
                totalSpellPages = 0;
                return;
            }

            RightListButton nextSchoolButton = new RightListButton(left + 165, top + 140) {
                @Override
                public void onPress() {
                    if (currentIndex + 1 < selectedList.size()) {
                        currentIndex++;
                        currentSpellPage = 0;
                        openLearningSpellList();
                    }
                }
            };

            LeftListButton backSchoolButton = new LeftListButton(left + 80, top + 140) {
                @Override
                public void onPress() {
                    if (currentIndex > 0) {
                        currentIndex--;
                        currentSpellPage = 0;
                        openLearningSpellList();
                    }
                }
            };

            UpListButton upButton = new UpListButton(left + ((SPELLS_COLS == 2) ? 55 : 32), top + 66 - 12) {
                @Override
                public void onPress() {
                    if (currentSpellPage > 0) {
                        currentSpellPage--;
                        updateSpellUI();
                    }
                }
            };

            DeployListButton deployListButton = new DeployListButton(left + ((SPELLS_COLS == 2) ? 55 : 32), top + 66) {
                @Override
                public void onPress() {
                    if (SPELLS_COLS == 2) SPELLS_COLS = 1;
                    else SPELLS_COLS = 2;

                    openLearningSpellList();
                }
            };

            DownListButton downButton = new DownListButton(left + ((SPELLS_COLS == 2) ? 55 : 32), top + 66 + 12) {
                @Override
                public void onPress() {
                    if (currentSpellPage < totalSpellPages - 1) {
                        currentSpellPage++;
                        updateSpellUI();
                    }
                }
            };
            addRenderableWidget(nextSchoolButton);
            addRenderableWidget(backSchoolButton);
            addRenderableWidget(upButton);
            addRenderableWidget(deployListButton);
            addRenderableWidget(downButton);

            updateSpellUI();
        }

        private void removeSpellButtons() {
            List<GuiEventListener> toRemove = new ArrayList<>();

            for (GuiEventListener widget : this.children()) {
                if (widget instanceof SpellButton ||
                    widget instanceof SmallLearnButton ||
                    widget instanceof SmallInfoButton ||
                    widget instanceof SmallConflictsButton ||
                    widget instanceof LeftLevelButton ||
                    widget instanceof RightLevelButton) {
                    toRemove.add(widget);
                }
            }

            for (GuiEventListener widget : toRemove) {
                removeWidget(widget);
            }
        }

        // rendering of spell buttons of the current school
        void renderSpells(SchoolType school) {
            if (school == null) return;

            final int START_X = left + 10;
            final int START_Y = top + 20;
            final int ICON_SIZE = 20;
            final int PADDING = 2;
            final int ROWS_MAX = 5;
            final int SPELLS_PER_PAGE = ROWS_MAX * SPELLS_COLS;

            // filter
            List<AbstractSpell> schoolSpells = allSpells.stream()
                    .filter(spell -> spell.getSchoolType().equals(school))
                    .toList();

            totalSpellPages = (int) Math.ceil((double) schoolSpells.size() / SPELLS_PER_PAGE);
            if (totalSpellPages == 0) totalSpellPages = 1;

            if (currentSpellPage >= totalSpellPages) {
                currentSpellPage = totalSpellPages - 1;
            }
            if (currentSpellPage < 0) currentSpellPage = 0;

            int x = START_X;
            int y = START_Y;
            int row = 0;
            int col = 0;

            removeSpellButtons();
            scrollOffset = 0;

            int startIndex = currentSpellPage * SPELLS_PER_PAGE;

            for (int i = startIndex; i < Math.min(startIndex + SPELLS_PER_PAGE, schoolSpells.size()); i++) {
                AbstractSpell spell = schoolSpells.get(i);

                if (col >= SPELLS_COLS) {
                    x = START_X;
                    y += ICON_SIZE + PADDING;
                    row++;
                    col = 0;

                    if (row >= ROWS_MAX) {
                        break;
                    }
                }

                boolean isLearned = learnedSpells.contains(spell.getSpellResource());
                boolean isCurrentSpell = (currentSpell == spell);
                boolean isConflicting = SpellConflictManager.hasConflict(spell.getSpellResource(),
                        learnedSpells);

                SpellButton spellButton = new SpellButton(x, y, spell, isLearned, isConflicting, isCurrentSpell) {
                    @Override
                    public void onPress() {
                        currentSpell = spell;
                        hasPlayerItemForLearn();
                    }
                };
                addRenderableWidget(spellButton);

                x += ICON_SIZE + PADDING;
                col++;
            }

            if (currentSpell != null) {
                SmallLearnButton learnSkillButton = new SmallLearnButton(left + ((SPELLS_COLS == 2) ? 87 : 57), top + 60, font, isLearnButtonBlocked, learnedSpells.contains(currentSpell.getSpellResource())) {
                    @Override
                    public void onPress() {
                        boolean currentSpellLearned = currentSpell != null && learnedSpells.contains(currentSpell.getSpellResource());

                        if (!isLearnButtonBlocked && !currentSpellLearned) {
                            if (getMenu().getTableSlotItem().getCount() > 0) {
                                if (Screen.hasShiftDown()) {
                                    resetProgress();
                                    learnSpell();
                                    playFinalSound();
                                    spellLearned = true;
                                    return;
                                }

                                isButtonPressed = true;
                                pressStartTime = System.currentTimeMillis();
                                fillProgress = 0f;
                                currentScale = 0;
                                spellLearned = false;
                                finalSoundPlayed = false;
                                lastSoundTime = 0L;
                            }
                        }
                    }

                    @Override
                    public void onRelease(double p_93669_, double p_93670_) {
                        resetProgress();
                    }
                };
                SmallInfoButton infoButton = new SmallInfoButton(left + ((SPELLS_COLS == 2) ? 102 : 72), top + 60, font, (currentSpellSmallInfo == 0)) {
                    @Override
                    public void onPress() {
                        currentSpellSmallInfo = 0;
                        updateSpellUI();
                    }
                };
                SmallConflictsButton conflictsButton = new SmallConflictsButton(left + ((SPELLS_COLS == 2) ? 117 : 87), top + 60, font, (currentSpellSmallInfo == 1)) {
                    @Override
                    public void onPress() {
                        currentSpellSmallInfo = 1;
                        updateSpellUI();
                    }
                };
                LeftLevelButton leftLevelButton = new LeftLevelButton((left + (256 - font.width(Component.translatable("ui.unraveling_spells.spell.level").getString()
                                + " " + currentSpellLvl)) / 2) + ((SPELLS_COLS == 2) ?
                                60 - 20 :
                                40 - 20),
                                currentSpellLvlY) {
                    @Override
                    public void onPress() {
                        currentSpellLvl = (currentSpellLvl <= 1 ? 1 : currentSpellLvl-1);
                        updateSpellUI();
                    }
                };

                RightLevelButton rightLevelButton = new RightLevelButton((left + (256 - font.width(Component.translatable("ui.unraveling_spells.spell.level").getString()
                                + " " + currentSpellLvl)) / 2) + ((SPELLS_COLS == 2) ?
                                60 + (font.width(Component.translatable("ui.unraveling_spells.spell.level").getString() + " " + currentSpellLvl)) + 15 :
                                40 + (font.width(Component.translatable("ui.unraveling_spells.spell.level").getString() + " " + currentSpellLvl)) + 15),
                                currentSpellLvlY) {
                    @Override
                    public void onPress() {
                        currentSpellLvl = (currentSpellLvl >= currentSpell.getMaxLevel() ? currentSpell.getMaxLevel() : currentSpellLvl+1);
                        updateSpellUI();
                    }
                };

                addRenderableWidget(leftLevelButton);
                addRenderableWidget(rightLevelButton);
                addRenderableWidget(learnSkillButton);
                addRenderableWidget(infoButton);
                addRenderableWidget(conflictsButton);
            }
        }

        public void hasPlayerItemForLearn() {
            this.isLearnButtonBlocked = true;
            ItemStack stackInSlot = getMenu().getTableSlotItem();

            if (currentSpell != null) {
                boolean isConflicting = SpellConflictManager.hasConflict(currentSpell.getSpellResource(),
                        learnedSpells);
                if (!stackInSlot.isEmpty() && stackInSlot.getCount() > 0) {
                    if (!isConflicting) {
                        this.isLearnButtonBlocked = false;
                    }
                }
                updateSpellUI();
            }
        }

        // page 1 rendering
        private void openLearnSchoolList() {
            if (isRenderingWidgets) {
                pendingLearnSchoolListUpdate = true;
                return;
            }

            removeAllWidgets();
            page = 1;

            for (int i = 0; i < VISIBLE_COUNT; i++) {
                int schoolIndex = currentIndex + i;
                if (schoolIndex >= schoolTypes.size()) break;

                SchoolType school = schoolTypes.get(schoolIndex);
                if (school == null) return;

                final Button[] detailsButtonRef = new Button[1];
                SchoolButton button = new SchoolButton(left + (i * (64+4)) + 28,
                        top + 55 - (selectedSchools.contains(school.getId()) ? 5 : 0), school) {
                    @Override
                    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float tics) {
                        /* 1.0.1 VERSION CODE
                        if (selectedSchools.contains(school.getId())) guiGraphics.blit(MAIN, getX(), getY(), 64, 208, 64, 48);
                        else guiGraphics.blit(MAIN, getX(), getY(), 64, 160, 64, 48);

                        String displayText = school.getDisplayName().getString();
                        guiGraphics.drawString(font, displayText, getX() + (64 - font.width(school.getDisplayName())) / 2, getY()+10, 0xD9CAD5, false);

                        // school's icon
                        List<AbstractSpell> schoolSpells = SpellRegistry.getSpellsForSchool(school);
                        if (!schoolSpells.isEmpty()) {
                            AbstractSpell spellIcon = schoolSpells.get(0);
                            guiGraphics.blit(
                                    spellIcon.getSpellIconResource(),
                                    getX() + 24, getY() + 25, 0, 0, 16, 16, 16, 16
                            );
                        }
                        if (selectedSchools.contains(school.getId())) guiGraphics.blit(MAIN, getX() + 24 - 3, getY() + 25 - 3, 128, 182, 22, 22);
                        else guiGraphics.blit(MAIN, getX() + 24 - 3, getY() + 25 - 3, 128, 160, 22, 22);
                         */

                        // 1.1.0 VERSION CODE
                        if (selectedSchools.contains(school.getId())) guiGraphics.blit(MAIN, getX(), getY(), 128, 160, 64, 80);
                        else guiGraphics.blit(MAIN, getX(), getY(), 64, 160, 64, 80);

                        //title
                        String displayText = school.getDisplayName().getString();
                        guiGraphics.drawString(font, displayText, getX() + (64 - font.width(school.getDisplayName())) / 2, getY()+10, 0xD9CAD5, true);

                        // school icon
                        guiGraphics.blit(getSchoolIcon(school), getX() + 16, getY() + 22, 0, 0, 32, 38, 32, 38);
                    }

                    @Override
                    public void onPress() {
                        ResourceLocation schoolId = school.getId();

                        if (selectedSchools.contains(schoolId)) {
                            selectedSchools.remove(schoolId);
                            AnimButtonYUp(this, top + 50, 5);
                            AnimButtonYUp(detailsButtonRef[0], top + 112, 5);
                        } else {
                            if (selectedSchools.size() < maxSchools) {
                                selectedSchools.add(schoolId);
                                AnimButtonYDown(this, top + 55, 5);
                                AnimButtonYDown(detailsButtonRef[0],top + 117, 5);
                            }
                        }
                    }
                };
                addRenderableWidget(button);

                //Details button
                Button detailsButton = new Button(new Button.Builder(Component.literal(""), button1 -> {})
                        .pos(button.getX() + (64 - 58) / 2, button.getY() + 62)
                        .size(58, 12)) {
                    @Override
                    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
                        if (!isMouseOver(mouseX,mouseY)) guiGraphics.blit(MAIN, getX(), getY(), 0, 224, 58, 12);
                        if (isMouseOver(mouseX,mouseY)) guiGraphics.blit(MAIN, getX(), getY(), 0, 240, 58, 12);
                        //guiGraphics.drawString(font, Component.literal("Details"),
                        //        getX() + (58 - font.width(Component.literal("Details"))) / 2, getY()+2, 0xD9CAD5, true);
                    }

                    @Override
                    public boolean isMouseOver(double mouseX, double mouseY) {
                        return mouseX >= getX() && mouseX < getX() + getWidth()
                                && mouseY >= getY() && mouseY < getY() + getHeight();
                    }

                    @Override
                    public void onPress() {
                        openDetailsSchool(school);
                    }
                };

                detailsButtonRef[0] = detailsButton;
                addRenderableWidget(detailsButton);
            }

            if (currentIndex + VISIBLE_COUNT < schoolTypes.size()) {
                RightListButton nextButton = new RightListButton(left + ((256 / 2) + (int) (256*0.4)),
                        top + 85) {
                    @Override
                    public void onPress() {
                        currentIndex += VISIBLE_COUNT;
                        openLearnSchoolList();
                    }
                };
                addRenderableWidget(nextButton);
            }

            if (currentIndex > 0) {
                LeftListButton backButton = new LeftListButton(left + ((256 / 2) - (int) (256*0.4) - 9*2),
                        top + 85) {
                    @Override
                    public void onPress() {
                        currentIndex -= VISIBLE_COUNT;
                        if (currentIndex < 0) currentIndex = 0;
                        openLearnSchoolList();
                    }
                };
                addRenderableWidget(backButton);
            }

            DefaultButton confirmButton =
                    new DefaultButton(left + (256 - 64) / 2, top + 140, getMinecraft().font,
                            Component.translatable("ui.unraveling_spells.button.done"), MAIN, 0, 192, 64, 16, 256, 256, 0, 208) {

                        @Override
                        public boolean isActive() {
                            if (selectedSchools.size() == maxSchools) {
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public void onPress() {
                            if (isActive()) {
                                List<ResourceLocation> selectedSchoolsList = new ArrayList<>(selectedSchools);
                                ModMessages.sendToServer(new SchoolC2SPacket(selectedSchoolsList));

                                onClose();
                            }
                        }
                    };

            addRenderableWidget(confirmButton);
        }

        private void openDetailsSchool(SchoolType school) {
            removeAllWidgets();
            detailsSchool = school;
            page = 3;

            DefaultButton backButton =
                    new DefaultButton(left + 13, top + 131, getMinecraft().font,
                            Component.literal("<"), MAIN, 0, 160, 16, 16, 256, 256, 0, 176) {

                        @Override
                        public void onPress() {
                            openLearnSchoolList();
                        }
                    };

            addRenderableWidget(backButton);
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

        private void removeAllWidgets() {
            List<GuiEventListener> toRemove = new ArrayList<>();

            for (GuiEventListener widget : this.children()) {
                if (widget instanceof SchoolButton ||
                        widget instanceof RightListButton ||
                        widget instanceof LeftListButton ||
                        widget instanceof DefaultButton ||
                        widget instanceof UpListButton ||
                        widget instanceof DeployListButton ||
                        widget instanceof DownListButton ||
                        widget instanceof SpellButton ||
                        widget instanceof Button) {
                    toRemove.add(widget);
                }
            }

            for (GuiEventListener widget : toRemove) {
                removeWidget(widget);
            }
        }

        private void openList() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            if (isSyncing) {
                return;
            }

            if (isInitialized) {
                updateUIFromSyncedData();
                return;
            }

            isSyncing = true;
            isSyncingSchool = true;
            isSyncingSpell = true;
            syncedSchoolIds.clear();
            syncedSpellIds.clear();
            ModMessages.sendToServer(new RequestSyncPacket());
        }

        private void updateSpellUI() {
            if (isRenderingWidgets) {
                pendingSpellUiUpdate = true;
                return;
            }

            renderSpells(currentSchool);
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
            learnedSpells.addAll(SpellLearnedManager.getDefaultLearnedSpells());

            if (selectedSchools.size() < maxSchools) {
                page = 1;
                openLearnSchoolList();
            } else {
                page = 2;
                openLearningSpellList();
            }
        }

        public void onSyncComplete() {
            if (isSyncingSchool || isSyncingSpell) {
                return;
            }

            isSyncing = false;
            isInitialized = true;

            updateUIFromSyncedData();
            if (blockEntity != null) {
                blockEntity.load(blockEntity.saveWithFullMetadata());
            }
        }

        public void SyncSchool() {
            isSyncingSchool = false;
        }

        public void SyncSchool(List<ResourceLocation> schools) {
            syncedSchoolIds.clear();
            syncedSchoolIds.addAll(schools);
            SyncSchool();
        }

        public void SyncSpell() {
            isSyncingSpell = false;
        }

        public void SyncSpell(List<ResourceLocation> spells) {
            syncedSpellIds.clear();
            syncedSpellIds.addAll(spells);
            SyncSpell();
        }

        private void AnimButtonYUp(Button button, int from, int step) {
            if (button == null) {
                return;
            }
            AnimationCompat.animate(this, from, from+step, 0.5f, AnimationCompat.Easing.EASE_IN_OUT, y -> button.setY(Math.round(y)));
        }

        private void AnimButtonYDown(Button button, int from, int step) {
            if (button == null) {
                return;
            }
            AnimationCompat.animate(this, from, from-step, 0.5f, AnimationCompat.Easing.EASE_IN_OUT, y -> button.setY(Math.round(y)));
        }

        @Override
        public void removed() {
            AnimationCompat.clear(this);
            super.removed();
        }
    }
