package org.holy.unraveling_spells.client.buttons;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.holy.unraveling_spells.Unraveling_spells;

public abstract class HoldConfirmButton extends ClassicButton {
    private static final long HOLD_DURATION_MS = 3000L;
    private static final int FILL_SOUND_STEPS = 3;

    private boolean holding;
    private long holdStartedAt;
    private float holdProgress;
    private int playedFillSounds;

    public HoldConfirmButton(int x, int y, int width, int height, Component title) {
        super(x, y, width, height, title);
    }

    @Override
    public void onPress() {
        if (!isActive() || !canStartHolding()) return;

        if (Screen.hasShiftDown()) {
            cancelHolding();
            confirm();
            return;
        }

        holding = true;
        holdStartedAt = Util.getMillis();
        holdProgress = 0.0f;
        playedFillSounds = 0;
        playNextFillSound();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        cancelHolding();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float ticks) {
        updateHolding();
        super.renderWidget(guiGraphics, mouseX, mouseY, ticks);
    }

    @Override
    protected float getFillProgress() {
        return holdProgress;
    }

    private void updateHolding() {
        if (!holding) return;
        if (!isActive()) {
            cancelHolding();
            return;
        }

        long elapsed = Util.getMillis() - holdStartedAt;
        holdProgress = Math.min(1.0f, elapsed / (float) HOLD_DURATION_MS);

        int expectedFillSounds = Math.min(FILL_SOUND_STEPS,
                1 + (int) (holdProgress * FILL_SOUND_STEPS));
        while (playedFillSounds < expectedFillSounds) {
            playNextFillSound();
        }

        if (holdProgress >= 1.0f) {
            confirm();
        }
    }

    private void confirm() {
        holding = false;
        holdProgress = 0.0f;
        playLearnSound();
        onConfirmed();
    }

    private void cancelHolding() {
        holding = false;
        holdStartedAt = 0L;
        holdProgress = 0.0f;
        playedFillSounds = 0;
    }

    private void playNextFillSound() {
        playedFillSounds++;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            float progress = playedFillSounds / (float) FILL_SOUND_STEPS;
            player.playSound(Unraveling_spells.SPELL_FILL.get(), 0.8f, 0.9f + progress * 0.2f);
        }
    }

    private void playLearnSound() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(Unraveling_spells.SPELL_LEARN.get(), 1.0f, 1.0f);
        }
    }

    protected boolean canStartHolding() {
        return true;
    }

    protected abstract void onConfirmed();
}
