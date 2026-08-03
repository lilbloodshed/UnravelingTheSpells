package org.holy.unraveling_spells.client.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScrollableTextArea {
    private static final int LINE_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_GAP = 5;
    private static final int MIN_THUMB_HEIGHT = 10;

    private final Font font;
    private Component text = Component.empty();
    private List<FormattedCharSequence> lines = List.of();
    private int x;
    private int y;
    private int width;
    private int height;
    private int textColor;
    private double scrollOffset;
    private boolean draggingScrollbar;
    private double scrollbarDragOffset;

    public ScrollableTextArea(Font font, int x, int y, int width, int height, int textColor) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textColor = textColor;
        rebuildLines();
    }

    public void setText(Component text) {
        this.text = text == null ? Component.empty() : text;
        scrollOffset = 0.0;
        rebuildLines();
    }

    public void setBounds(int x, int y, int width, int height) {
        int newWidth = Math.max(1, width);
        int newHeight = Math.max(1, height);
        boolean boundsChanged = this.width != newWidth || this.height != newHeight;
        this.x = x;
        this.y = y;
        this.width = newWidth;
        this.height = newHeight;
        if (boundsChanged) {
            rebuildLines();
        } else {
            clampScroll();
        }
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void resetScroll() {
        scrollOffset = 0.0;
        draggingScrollbar = false;
    }

    public boolean hasScrollbar() {
        return getMaxScroll() > 0;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int lineStep = getLineStep();
        int firstLineY = y - (int) Math.round(scrollOffset);

        guiGraphics.enableScissor(x, y, x + getTextWidth(), y + height);
        for (int index = 0; index < lines.size(); index++) {
            int lineY = firstLineY + index * lineStep;
            if (lineY + font.lineHeight >= y && lineY < y + height) {
                guiGraphics.drawString(font, lines.get(index), x, lineY, textColor, false);
            }
        }
        guiGraphics.disableScissor();

        if (hasScrollbar()) {
            int scrollbarX = getScrollbarX();
            int thumbY = getThumbY();
            int thumbHeight = getThumbHeight();
            boolean hovered = mouseX >= scrollbarX - 1 && mouseX < scrollbarX + SCROLLBAR_WIDTH + 1
                    && mouseY >= y && mouseY < y + height;

            guiGraphics.fill(scrollbarX, y, scrollbarX + SCROLLBAR_WIDTH, y + height, 0x603E3540);
            guiGraphics.fill(scrollbarX, thumbY,
                    scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight,
                    hovered || draggingScrollbar ? 0xFFD9CAD5 : 0xFF786D76);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!hasScrollbar() || !isMouseOver(mouseX, mouseY)) return false;
        setScrollOffset(scrollOffset - delta * getLineStep() * 3.0);
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !hasScrollbar()) return false;

        int scrollbarX = getScrollbarX();
        if (mouseX < scrollbarX - 2 || mouseX >= scrollbarX + SCROLLBAR_WIDTH + 2
                || mouseY < y || mouseY >= y + height) {
            return false;
        }

        int thumbY = getThumbY();
        int thumbHeight = getThumbHeight();
        if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
            draggingScrollbar = true;
            scrollbarDragOffset = mouseY - thumbY;
        } else {
            scrollToThumbPosition(mouseY - thumbHeight / 2.0);
            draggingScrollbar = true;
            scrollbarDragOffset = thumbHeight / 2.0;
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (button != 0 || !draggingScrollbar) return false;
        scrollToThumbPosition(mouseY - scrollbarDragOffset);
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != 0 || !draggingScrollbar) return false;
        draggingScrollbar = false;
        return true;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Nullable
    public Style getStyleAtPosition(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;

        int localX = (int) Math.floor(mouseX - x);
        if (localX < 0 || localX >= getTextWidth()) return null;

        int contentY = (int) Math.floor(mouseY - y) + (int) Math.round(scrollOffset);
        int lineStep = getLineStep();
        int lineIndex = contentY / lineStep;
        int yInsideLine = contentY - lineIndex * lineStep;
        if (lineIndex < 0 || lineIndex >= lines.size()
                || yInsideLine < 0 || yInsideLine >= font.lineHeight) {
            return null;
        }

        return font.getSplitter().componentStyleAtWidth(lines.get(lineIndex), localX);
    }

    private void rebuildLines() {
        int fullWidth = Math.max(1, width);
        lines = font.split(text, fullWidth);
        if (getContentHeight() > height) {
            lines = font.split(text, Math.max(1, fullWidth - SCROLLBAR_WIDTH - SCROLLBAR_GAP));
        }
        clampScroll();
    }

    private int getTextWidth() {
        return hasScrollbar()
                ? Math.max(1, width - SCROLLBAR_WIDTH - SCROLLBAR_GAP)
                : width;
    }

    private int getLineStep() {
        return font.lineHeight + LINE_GAP;
    }

    private int getContentHeight() {
        if (lines.isEmpty()) return 0;
        return lines.size() * getLineStep() - LINE_GAP;
    }

    private int getMaxScroll() {
        return Math.max(0, getContentHeight() - height);
    }

    private int getScrollbarX() {
        return x + width - SCROLLBAR_WIDTH;
    }

    private int getThumbHeight() {
        return Math.max(MIN_THUMB_HEIGHT,
                Math.min(height, Math.round(height * (height / (float) getContentHeight()))));
    }

    private int getThumbY() {
        int thumbTravel = height - getThumbHeight();
        if (thumbTravel <= 0 || getMaxScroll() <= 0) return y;
        return y + (int) Math.round(thumbTravel * (scrollOffset / getMaxScroll()));
    }

    private void scrollToThumbPosition(double thumbY) {
        int thumbTravel = height - getThumbHeight();
        if (thumbTravel <= 0) {
            setScrollOffset(0.0);
            return;
        }
        double ratio = (thumbY - y) / thumbTravel;
        setScrollOffset(ratio * getMaxScroll());
    }

    private void setScrollOffset(double scrollOffset) {
        this.scrollOffset = Math.max(0.0, Math.min(getMaxScroll(), scrollOffset));
    }

    private void clampScroll() {
        setScrollOffset(scrollOffset);
    }
}
