package dev.fweigel.shulkerboxutils.mixin;

import dev.fweigel.shulkerboxutils.ShulkerBoxUtilsConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class ShulkerInventoryMixin {

    // Prevents infinite recursion when we call item() to draw the badge.
    @Unique
    private static final ThreadLocal<Boolean> spRendering = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
        at = @At("TAIL")
    )
    private void renderShulkerDecorations(LivingEntity entity, Level level, ItemStack stack,
                                          int x, int y, int seed, CallbackInfo ci) {
        if (spRendering.get()) return;
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof BlockItem bi)) return;
        if (!(bi.getBlock() instanceof ShulkerBoxBlock)) return;

        boolean badgeEnabled = ShulkerBoxUtilsConfig.isBadgeEnabled();
        boolean fillEnabled = ShulkerBoxUtilsConfig.isFillIndicatorEnabled();
        if (!badgeEnabled && !fillEnabled) return;

        GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;

        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        int filledSlots = 0;
        ItemStack firstItem = ItemStack.EMPTY;
        boolean uniform = true;
        net.minecraft.world.item.Item singleType = null;

        if (contents != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            contents.copyInto(items);
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    filledSlots++;
                    if (firstItem.isEmpty()) firstItem = item;
                    if (singleType == null) singleType = item.getItem();
                    else if (singleType != item.getItem()) uniform = false;
                }
            }
        }

        // --- Item badge ---
        if (badgeEnabled && !firstItem.isEmpty()) {
            if (!ShulkerBoxUtilsConfig.isUniformOnly() || uniform) {
                var pose = self.pose();
                pose.pushMatrix();
                pose.translate(x + 4, y + 4);
                pose.scale(0.5f, 0.5f);
                spRendering.set(true);
                try {
                    self.item(firstItem, 0, 0);
                } finally {
                    spRendering.set(false);
                    pose.popMatrix();
                }
            }
        }

        // --- Fill indicator bar ---
        // Inverted durability convention: green = empty (like new tool), red = full (like broken tool)
        if (fillEnabled) {
            float fillFraction = filledSlots / 27.0f;
            float displayFraction = 1.0f - fillFraction;
            int barWidth = Math.max(1, Math.round(13.0f * displayFraction));
            int rgb = Mth.hsvToRgb(displayFraction / 3.0f, 1.0f, 1.0f);
            // 2px black background (matches vanilla durability bar layout)
            self.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
            // Colored fill bar on top row; bottom row stays black as shadow (same as vanilla)
            self.fill(x + 2, y + 13, x + 2 + barWidth, y + 14, 0xFF000000 | rgb);
        }
    }
}
