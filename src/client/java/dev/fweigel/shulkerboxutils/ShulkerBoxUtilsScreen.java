package dev.fweigel.shulkerboxutils;

import dev.fweigel.mobutils.core.client.ui.ModScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShulkerBoxUtilsScreen extends ModScreen {

    // Source screenshot size — all preview PNGs must be exactly this.
    private static final int SRC_W = 320;
    private static final int SRC_H = 180;

    // Card preview size: 96×54 is exactly 16:9 and matches HALF_BUTTON_WIDTH.
    // Scale factor is a clean 0.3× (96/320 = 54/180 = 0.3), so no distortion.
    private static final int CARD_W        = 96;
    private static final int CARD_PREV_H   = 54;
    private static final int CARD_BTN_GAP  = 2;
    private static final int CARD_H        = CARD_PREV_H + CARD_BTN_GAP + BUTTON_HEIGHT; // 76

    private static final int COL_GAP  = 8;   // gap between left and right card columns
    private static final int ROW_GAP  = 8;   // gap between top and bottom card rows

    // Preview textures (source 320×180 px)
    private static final Identifier IMG_BADGE_ON  = Identifier.parse("shulkerboxutils:textures/gui/preview/badge_on.png");
    private static final Identifier IMG_BADGE_OFF = Identifier.parse("shulkerboxutils:textures/gui/preview/badge_off.png");
    private static final Identifier IMG_WORLD_ON  = Identifier.parse("shulkerboxutils:textures/gui/preview/world_icon_on.png");
    private static final Identifier IMG_WORLD_OFF = Identifier.parse("shulkerboxutils:textures/gui/preview/world_icon_off.png");
    private static final Identifier IMG_FILL_ON   = Identifier.parse("shulkerboxutils:textures/gui/preview/fill_indicator_on.png");
    private static final Identifier IMG_FILL_OFF  = Identifier.parse("shulkerboxutils:textures/gui/preview/fill_indicator_off.png");
    private static final Identifier IMG_CONT_ON   = Identifier.parse("shulkerboxutils:textures/gui/preview/contents_preview_on.png");
    private static final Identifier IMG_CONT_OFF  = Identifier.parse("shulkerboxutils:textures/gui/preview/contents_preview_off.png");

    private Button badgeToggle;
    private Button worldIconToggle;
    private Button fillIndicatorToggle;
    private Button contentsPreviewToggle;
    private Button uniformOnlyToggle;

    public ShulkerBoxUtilsScreen() {
        super(Component.translatable("shulkerboxutils.screen.title"));
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int colL = cx - BUTTON_WIDTH / 2;          // left column  x = cx - 100
        int colR = colL + CARD_W + COL_GAP;        // right column x = cx + 4
        int row1 = 25;                             // top card row y
        int row2 = row1 + CARD_H + ROW_GAP;        // bottom card row y = 109
        int btnY1 = row1 + CARD_PREV_H + CARD_BTN_GAP;  // button y for row 1 = 81
        int btnY2 = row2 + CARD_PREV_H + CARD_BTN_GAP;  // button y for row 2 = 165

        // ── Row 1: Badge (left) ─ World Icon (right) ──────────────────────────
        badgeToggle = addRenderableWidget(Button.builder(
                getBadgeLabel(),
                b -> { ShulkerBoxUtilsConfig.toggleBadge(); badgeToggle.setMessage(getBadgeLabel()); ShulkerBoxUtilsStorage.save(); }
        ).bounds(colL, btnY1, CARD_W, BUTTON_HEIGHT).build());

        worldIconToggle = addRenderableWidget(Button.builder(
                getWorldIconLabel(),
                b -> { ShulkerBoxUtilsConfig.toggleWorldIcon(); worldIconToggle.setMessage(getWorldIconLabel()); ShulkerBoxUtilsStorage.save(); }
        ).bounds(colR, btnY1, CARD_W, BUTTON_HEIGHT).build());

        // ── Row 2: Fill Indicator (left) ─ Contents Preview (right) ──────────
        fillIndicatorToggle = addRenderableWidget(Button.builder(
                getFillLabel(),
                b -> { ShulkerBoxUtilsConfig.toggleFillIndicator(); fillIndicatorToggle.setMessage(getFillLabel()); ShulkerBoxUtilsStorage.save(); }
        ).bounds(colL, btnY2, CARD_W, BUTTON_HEIGHT).build());

        contentsPreviewToggle = addRenderableWidget(Button.builder(
                getContentsLabel(),
                b -> { ShulkerBoxUtilsConfig.toggleContentsPreview(); contentsPreviewToggle.setMessage(getContentsLabel()); ShulkerBoxUtilsStorage.save(); }
        ).bounds(colR, btnY2, CARD_W, BUTTON_HEIGHT).build());

        // ── Display Mode: full-width below the grid ────────────────────────────
        int displayModeY = row2 + CARD_H + 10;
        uniformOnlyToggle = addRenderableWidget(Button.builder(
                getUniformOnlyLabel(),
                b -> { ShulkerBoxUtilsConfig.toggleUniformOnly(); uniformOnlyToggle.setMessage(getUniformOnlyLabel()); ShulkerBoxUtilsStorage.save(); }
        ).bounds(colL, displayModeY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // ── Done ───────────────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                b -> this.onClose()
        ).bounds(colL, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        int cx   = this.width / 2;
        int colL = cx - BUTTON_WIDTH / 2;
        int colR = colL + CARD_W + COL_GAP;
        int row1 = 25;
        int row2 = row1 + CARD_H + ROW_GAP;

        drawCardPreview(graphics, colL, row1, ShulkerBoxUtilsConfig.isBadgeEnabled()          ? IMG_BADGE_ON  : IMG_BADGE_OFF);
        drawCardPreview(graphics, colR, row1, ShulkerBoxUtilsConfig.isWorldIconEnabled()       ? IMG_WORLD_ON  : IMG_WORLD_OFF);
        drawCardPreview(graphics, colL, row2, ShulkerBoxUtilsConfig.isFillIndicatorEnabled()   ? IMG_FILL_ON   : IMG_FILL_OFF);
        drawCardPreview(graphics, colR, row2, ShulkerBoxUtilsConfig.isContentsPreviewEnabled() ? IMG_CONT_ON   : IMG_CONT_OFF);
    }

    private void drawCardPreview(GuiGraphicsExtractor graphics, int x, int y, Identifier texture) {
        // 1px black border around the preview area
        graphics.fill(x - 1, y - 1, x + CARD_W + 1, y + CARD_PREV_H + 1, 0xFF000000);
        // Scale 320×180 → 96×54 (factor 0.3 exactly, no distortion)
        final float S = 0.3f;
        graphics.pose().pushMatrix();
        graphics.pose().scale(S, S);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                Math.round(x / S), Math.round(y / S),
                0f, 0f, SRC_W, SRC_H, SRC_W, SRC_H);
        graphics.pose().popMatrix();
    }

    // ── Button label helpers ───────────────────────────────────────────────────

    private Component getBadgeLabel() {
        return Component.translatable("shulkerboxutils.screen.badge.card",
                state(ShulkerBoxUtilsConfig.isBadgeEnabled()));
    }

    private Component getWorldIconLabel() {
        return Component.translatable("shulkerboxutils.screen.world_icon.card",
                state(ShulkerBoxUtilsConfig.isWorldIconEnabled()));
    }

    private Component getFillLabel() {
        return Component.translatable("shulkerboxutils.screen.fill_indicator.card",
                state(ShulkerBoxUtilsConfig.isFillIndicatorEnabled()));
    }

    private Component getContentsLabel() {
        return Component.translatable("shulkerboxutils.screen.contents_preview.card",
                state(ShulkerBoxUtilsConfig.isContentsPreviewEnabled()));
    }

    private Component getUniformOnlyLabel() {
        String mode = Component.translatable(ShulkerBoxUtilsConfig.isUniformOnly()
                ? "shulkerboxutils.screen.badge_mode.uniform"
                : "shulkerboxutils.screen.badge_mode.first").getString();
        return Component.translatable("shulkerboxutils.screen.uniform_only", mode);
    }

    private String state(boolean b) {
        return Component.translatable(b ? "shulkerboxutils.state.on" : "shulkerboxutils.state.off").getString();
    }
}
