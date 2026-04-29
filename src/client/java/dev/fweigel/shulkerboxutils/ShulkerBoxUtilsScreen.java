package dev.fweigel.shulkerboxutils;

import dev.fweigel.mobutils.core.client.ui.ModOptionsList;
import dev.fweigel.mobutils.core.client.ui.ModOptionsList.CardSpec;
import dev.fweigel.mobutils.core.client.ui.ModSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShulkerBoxUtilsScreen extends ModSettingsScreen {

    private static final Identifier IMG_BADGE_ON  = id("badge_on.png");
    private static final Identifier IMG_BADGE_OFF = id("badge_off.png");
    private static final Identifier IMG_WORLD_ON  = id("world_icon_on.png");
    private static final Identifier IMG_WORLD_OFF = id("world_icon_off.png");
    private static final Identifier IMG_FILL_ON   = id("fill_indicator_on.png");
    private static final Identifier IMG_FILL_OFF  = id("fill_indicator_off.png");
    private static final Identifier IMG_CONT_ON   = id("contents_preview_on.png");
    private static final Identifier IMG_CONT_OFF  = id("contents_preview_off.png");

    public ShulkerBoxUtilsScreen() {
        super(Component.translatable("shulkerboxutils.screen.title"));
    }

    @Override
    protected void addOptions(ModOptionsList list) {
        // ── Row 1: Badge (left) | World Icon (right) ──────────────────────────
        list.addSplitCard(
            CardSpec.image(() -> ShulkerBoxUtilsConfig.isBadgeEnabled()    ? IMG_BADGE_ON : IMG_BADGE_OFF),
            buildHalfButton(this::getBadgeLabel, () -> {
                ShulkerBoxUtilsConfig.toggleBadge();
                ShulkerBoxUtilsStorage.save();
            }),
            CardSpec.image(() -> ShulkerBoxUtilsConfig.isWorldIconEnabled() ? IMG_WORLD_ON : IMG_WORLD_OFF),
            buildHalfButton(this::getWorldIconLabel, () -> {
                ShulkerBoxUtilsConfig.toggleWorldIcon();
                ShulkerBoxUtilsStorage.save();
            })
        );

        // ── Row 2: Fill Indicator (left) | Contents Preview (right) ──────────
        list.addSplitCard(
            CardSpec.image(() -> ShulkerBoxUtilsConfig.isFillIndicatorEnabled()   ? IMG_FILL_ON : IMG_FILL_OFF),
            buildHalfButton(this::getFillLabel, () -> {
                ShulkerBoxUtilsConfig.toggleFillIndicator();
                ShulkerBoxUtilsStorage.save();
            }),
            CardSpec.image(() -> ShulkerBoxUtilsConfig.isContentsPreviewEnabled() ? IMG_CONT_ON : IMG_CONT_OFF),
            buildHalfButton(this::getContentsLabel, () -> {
                ShulkerBoxUtilsConfig.toggleContentsPreview();
                ShulkerBoxUtilsStorage.save();
            })
        );

        // ── Uniform only (full-width) ─────────────────────────────────────────
        list.addWide(buildWideButton(this::getUniformOnlyLabel, () -> {
            ShulkerBoxUtilsConfig.toggleUniformOnly();
            ShulkerBoxUtilsStorage.save();
        }));
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    private static Identifier id(String path) {
        return Identifier.parse("shulkerboxutils:textures/gui/preview/" + path);
    }

    private String state(boolean b) {
        return Component.translatable(b ? "shulkerboxutils.state.on" : "shulkerboxutils.state.off").getString();
    }

    // ── Label suppliers ────────────────────────────────────────────────────────

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
}
