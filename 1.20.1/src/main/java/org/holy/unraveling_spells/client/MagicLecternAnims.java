package org.holy.unraveling_spells.client;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.client.buttons.SpellButton;
import org.holy.unraveling_spells.compat.AnimationCompat;

import java.util.ArrayList;
import java.util.List;

public class MagicLecternAnims {
    private final MagicLecternScreen screen;

    // schools
    private final List<SchoolButtonAnimation> schoolButtonAnimations = new ArrayList<>();
    private final List<Button> schoolControlButtons = new ArrayList<>();
    private int nextSchoolButtonAnimationIndex;
    private long nextSchoolButtonAnimationAt;
    private long schoolListAnimationFinishesAt;

    // details
    private long schoolDetailsAnimationEndsAt;
    private int schoolDetailsAnimationId;

    // question window
    private long questionWindowAnimationEndsAt;
    private int questionWindowAnimationId;

    // transitions
    private long spellListTransitionEndsAt;

    // const
    private static final float SCHOOL_BUTTON_ANIMATION_DURATION = 0.2f;
    private static final long SCHOOL_BUTTON_ANIMATION_DURATION_MS = 200L;
    private static final long SCHOOL_BUTTON_ANIMATION_STAGGER_MS = 100L;
    private static final float SCHOOL_DETAILS_ANIMATION_DURATION = 0.2f;
    private static final long SCHOOL_DETAILS_ANIMATION_DURATION_MS = 200L;
    private static final float SPELL_LIST_ANIMATION_DURATION = 0.2f;
    private static final long SPELL_LIST_ANIMATION_DURATION_MS = 250L;
    private static final float SPELL_LIST_FADE_DURATION = 0.20f;
    private static final float SPELL_ARROW_FADE_DURATION = 0.24f;
    private static final float SCHOOL_PANEL_JUMP_HEIGHT = 5.0f;
    private static final float SCHOOL_PANEL_JUMP_DURATION = 0.12f;
    private static final float QUESTION_WINDOW_ANIMATION_DURATION = 0.2f;
    private static final long QUESTION_WINDOW_ANIMATION_DURATION_MS = 200L;

    public MagicLecternAnims(MagicLecternScreen screen) {
        this.screen = screen;
    }

    public void init() {
        schoolButtonAnimations.clear();
        schoolControlButtons.clear();
        screen.schoolListAnimating = false;
        nextSchoolButtonAnimationIndex = 0;
        nextSchoolButtonAnimationAt = 0L;
        schoolListAnimationFinishesAt = 0L;

        schoolDetailsAnimationEndsAt = 0L;
        schoolDetailsAnimationId = 0;

        questionWindowAnimationEndsAt = 0L;
        questionWindowAnimationId = 0;

        spellListTransitionEndsAt = 0L;
    }

    public void removed() {
        AnimationCompat.clear(screen);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        updateSchoolListAnimation();
        updateSpellListTransition();
        AnimationCompat.update(screen);
        updateSchoolDetailsWindowAnimation();
        updateQuestionWindowAnimation();
    }

    // SCHOOL ANIMATIONS

    public void resetSchoolButtonAnimation() {
        schoolButtonAnimations.clear();
        schoolControlButtons.clear();
    }

    public void addSchoolButtonAnimation(Button schoolButton, Button detailsButton, int targetSchoolX, int targetDetailsX) {
        schoolButtonAnimations.add(new SchoolButtonAnimation(
                schoolButton, detailsButton, targetSchoolX, targetDetailsX));
    }

    public void addSchoolControlButton(Button button) {
        schoolControlButtons.add(button);
    }

    public void startSchoolListAnimation() {
        if (!AnimationCompat.isEnabled() || schoolButtonAnimations.isEmpty()) {
            finishSchoolListAnimation();
            return;
        }

        screen.schoolListAnimating = true;
        nextSchoolButtonAnimationIndex = 0;
        long now = Util.getMillis();
        nextSchoolButtonAnimationAt = now;
        schoolListAnimationFinishesAt = now + (schoolButtonAnimations.size() - 1L) * SCHOOL_BUTTON_ANIMATION_STAGGER_MS + SCHOOL_BUTTON_ANIMATION_DURATION_MS;

        for (Button controlButton : schoolControlButtons) {
            AnimationCompat.animate(screen,
                    0.0f, 1.0f,
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    controlButton::setAlpha);
        }
    }

    private void updateSchoolListAnimation() {
        if (!screen.schoolListAnimating) return;

        if (!AnimationCompat.isEnabled()) {
            finishSchoolListAnimation();
            return;
        }

        long now = Util.getMillis();
        while (nextSchoolButtonAnimationIndex < schoolButtonAnimations.size()
                && now >= nextSchoolButtonAnimationAt) {
            SchoolButtonAnimation animation = schoolButtonAnimations.get(nextSchoolButtonAnimationIndex);

            AnimationCompat.animate(screen,
                    animation.schoolButton().getX(), animation.targetSchoolX(),
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    x -> animation.schoolButton().setX(Math.round(x)));
            AnimationCompat.animate(screen,
                    animation.detailsButton().getX(), animation.targetDetailsX(),
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    x -> animation.detailsButton().setX(Math.round(x)));
            AnimationCompat.animate(screen,
                    0.0f, 1.0f,
                    SCHOOL_BUTTON_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                    animation.schoolButton()::setAlpha);
            AnimationCompat.animate(screen,
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
        screen.schoolListAnimating = false;
    }

    // ANIMATIONS "DETAILS"

    public void openSchoolDetails(SchoolType school) {
        if (school == null || screen.schoolListAnimating) return;

        screen.schoolDetailed = school;
        screen.schoolDetailsWindowOpen = true;
        screen.schoolDetailsWindowClosing = false;
        screen.questionWindowOpen = false;
        int animationId = ++schoolDetailsAnimationId;

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            screen.schoolDetailsWindowProgress = 1.0f;
            return;
        }

        screen.schoolDetailsWindowProgress = 0.0f;
        AnimationCompat.animate(screen,
                0.0f, 1.0f,
                SCHOOL_DETAILS_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                progress -> {
                    if (animationId == schoolDetailsAnimationId) {
                        screen.schoolDetailsWindowProgress = progress;
                    }
                });
    }

    public void closeSchoolDetails() {
        if (!screen.schoolDetailsWindowOpen || screen.schoolDetailsWindowClosing) return;

        int animationId = ++schoolDetailsAnimationId;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            finishClosingSchoolDetails(animationId);
            return;
        }

        screen.schoolDetailsWindowClosing = true;
        schoolDetailsAnimationEndsAt = Util.getMillis() + SCHOOL_DETAILS_ANIMATION_DURATION_MS;
        AnimationCompat.animate(screen,
                screen.schoolDetailsWindowProgress, 0.0f,
                SCHOOL_DETAILS_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                progress -> {
                    if (animationId == schoolDetailsAnimationId) {
                        screen.schoolDetailsWindowProgress = progress;
                    }
                });
    }

    private void updateSchoolDetailsWindowAnimation() {
        if (screen.schoolDetailsWindowClosing
                && Util.getMillis() >= schoolDetailsAnimationEndsAt) {
            finishClosingSchoolDetails(schoolDetailsAnimationId);
        }
    }

    private void finishClosingSchoolDetails(int animationId) {
        if (animationId != schoolDetailsAnimationId) return;

        screen.schoolDetailsWindowProgress = 0.0f;
        screen.schoolDetailsWindowClosing = false;
        screen.schoolDetailsWindowOpen = false;
        screen.schoolDetailed = null;
    }

    // ANIMATIONS QUESTION WINDOW

    public void openQuestionWindow() {
        screen.questionWindowOpen = true;
        screen.questionWindowClosing = false;
        int animationId = ++questionWindowAnimationId;

        if (screen.questionTextArea != null) {
            screen.questionTextArea.setText(Component.translatable("ui.unraveling_spells.question.text"));
            screen.questionTextArea.resetScroll();
        }

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            screen.questionWindowProgress = 1.0f;
            return;
        }

        screen.questionWindowProgress = 0.0f;
        AnimationCompat.animate(screen,
                0.0f, 1.0f,
                QUESTION_WINDOW_ANIMATION_DURATION, AnimationCompat.Easing.EASE_OUT,
                progress -> {
                    if (animationId == questionWindowAnimationId) {
                        screen.questionWindowProgress = progress;
                    }
                });
    }

    public void closeQuestionWindow() {
        if (!screen.questionWindowOpen || screen.questionWindowClosing) return;

        int animationId = ++questionWindowAnimationId;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            finishClosingQuestionWindow(animationId);
            return;
        }

        screen.questionWindowClosing = true;
        questionWindowAnimationEndsAt = Util.getMillis() + QUESTION_WINDOW_ANIMATION_DURATION_MS;
        AnimationCompat.animate(screen,
                screen.questionWindowProgress, 0.0f,
                QUESTION_WINDOW_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                progress -> {
                    if (animationId == questionWindowAnimationId) {
                        screen.questionWindowProgress = progress;
                    }
                });
    }

    private void updateQuestionWindowAnimation() {
        if (screen.questionWindowClosing && Util.getMillis() >= questionWindowAnimationEndsAt) {
            finishClosingQuestionWindow(questionWindowAnimationId);
        }
    }

    private void finishClosingQuestionWindow(int animationId) {
        if (animationId != questionWindowAnimationId) return;

        screen.questionWindowProgress = 0.0f;
        screen.questionWindowClosing = false;
        screen.questionWindowOpen = false;
    }

    // animations for switching the spell list

    public void switchSchoolWithSpellListAnimation(SchoolType targetSchool) {
        if (targetSchool == null || targetSchool == screen.currentSchool ||
                screen.spellListTransition != MagicLecternScreen.SpellListTransition.NONE) return;

        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            screen.currentSchool = targetSchool;
            screen.currentSpellPage = 0;
            screen.currentSpell = null;
            screen.learningSpellsTab();
            return;
        }

        screen.pendingSchool = targetSchool;
        screen.spellListTransition = MagicLecternScreen.SpellListTransition.EXITING;
        spellListTransitionEndsAt = Util.getMillis() + SPELL_LIST_ANIMATION_DURATION_MS;
        if (screen.previousSchoolSwitchButton != null) screen.previousSchoolSwitchButton.active = false;
        if (screen.nextSchoolSwitchButton != null) screen.nextSchoolSwitchButton.active = false;
        fadeOutSpellPageButtons();

        AnimationCompat.animate(screen,
                screen.schoolPanelYOffset, -SCHOOL_PANEL_JUMP_HEIGHT,
                SCHOOL_PANEL_JUMP_DURATION, AnimationCompat.Easing.EASE_OUT,
                value -> screen.schoolPanelYOffset = value);

        for (SpellButton button : screen.visibleSpellButtons) {
            button.active = false;
            AnimationCompat.animate(screen,
                    button.getY(), button.getY() + screen.SPELL_LIST_ANIMATION_OFFSET,
                    SPELL_LIST_ANIMATION_DURATION, AnimationCompat.Easing.EASE_IN,
                    value -> button.setY(Math.round(value)));
            AnimationCompat.animate(screen,
                    1.0f, 0.0f,
                    SPELL_LIST_FADE_DURATION, AnimationCompat.Easing.LINEAR,
                    button::setAlpha);
        }
    }

    private void fadeOutSpellPageButtons() {
        if (screen.previousSpellPageButton != null) {
            screen.previousSpellPageButton.active = false;
            AnimationCompat.animate(screen,
                    1.0f, 0.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_IN,
                    screen.previousSpellPageButton::setAlpha);
        }
        if (screen.nextSpellPageButton != null) {
            screen.nextSpellPageButton.active = false;
            AnimationCompat.animate(screen,
                    1.0f, 0.0f,
                    SPELL_ARROW_FADE_DURATION, AnimationCompat.Easing.EASE_IN,
                    screen.nextSpellPageButton::setAlpha);
        }
    }

    private void updateSpellListTransition() {
        if (screen.spellListTransition != MagicLecternScreen.SpellListTransition.NONE && !AnimationCompat.isEnabled()) {
            if (screen.pendingSchool != null) {
                screen.currentSchool = screen.pendingSchool;
                screen.pendingSchool = null;
                screen.currentSpellPage = 0;
                screen.currentSpell = null;
            }
            screen.spellListTransition = MagicLecternScreen.SpellListTransition.NONE;
            screen.schoolPanelYOffset = 0.0f;
            screen.learningSpellsTab();
            return;
        }

        if (screen.spellListTransition == MagicLecternScreen.SpellListTransition.NONE ||
                Util.getMillis() < spellListTransitionEndsAt) return;

        int rowY = screen.top + screen.panelHeight - screen.SPELL_BUTTON_SIZE - 7;

        if (screen.spellListTransition == MagicLecternScreen.SpellListTransition.EXITING) {
            for (SpellButton button : screen.visibleSpellButtons) {
                button.setY(rowY + screen.SPELL_LIST_ANIMATION_OFFSET);
                button.setAlpha(0.0f);
            }

            screen.currentSchool = screen.pendingSchool;
            screen.pendingSchool = null;
            screen.currentSpellPage = 0;
            screen.currentSpell = null;
            screen.spellListTransition = MagicLecternScreen.SpellListTransition.ENTERING;
            spellListTransitionEndsAt = Util.getMillis() + SPELL_LIST_ANIMATION_DURATION_MS;
            screen.schoolPanelYOffset = -SCHOOL_PANEL_JUMP_HEIGHT;
            screen.learningSpellsTab();
            AnimationCompat.animate(screen,
                    -SCHOOL_PANEL_JUMP_HEIGHT, 0.0f,
                    SCHOOL_PANEL_JUMP_DURATION, AnimationCompat.Easing.EASE_OUT,
                    value -> screen.schoolPanelYOffset = value);
            return;
        }

        for (SpellButton button : screen.visibleSpellButtons) {
            button.setY(rowY);
            button.setAlpha(1.0f);
            button.active = true;
        }

        screen.spellListTransition = MagicLecternScreen.SpellListTransition.NONE;
        screen.schoolPanelYOffset = 0.0f;
        if (screen.previousSpellPageButton != null) screen.previousSpellPageButton.active = true;
        if (screen.nextSpellPageButton != null) screen.nextSpellPageButton.active = true;
        if (screen.previousSchoolSwitchButton != null) screen.previousSchoolSwitchButton.active = true;
        if (screen.nextSchoolSwitchButton != null) screen.nextSchoolSwitchButton.active = true;
    }

    // auxiliary methodss

    public void animButtonYTo(Button button, int targetY) {
        if (button == null) return;
        if (Screen.hasShiftDown() || !AnimationCompat.isEnabled()) {
            button.setY(targetY);
            return;
        }
        AnimationCompat.animate(screen,
                button.getY(),
                targetY,
                0.35f,
                AnimationCompat.Easing.EASE_OUT,
                y -> button.setY(Math.round(y)));
    }

    private record SchoolButtonAnimation(Button schoolButton, Button detailsButton, int targetSchoolX, int targetDetailsX) { }
}