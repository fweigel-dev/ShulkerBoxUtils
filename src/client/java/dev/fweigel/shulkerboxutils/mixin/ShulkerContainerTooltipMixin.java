package dev.fweigel.shulkerboxutils.mixin;

import dev.fweigel.shulkerboxutils.ShulkerBoxUtilsConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ShulkerContainerTooltipMixin {

    /**
     * When Contents Preview is active, suppress the vanilla "N items inside" description
     * that ItemContainerContents adds via the CONTAINER component. The preview tooltip
     * already shows everything visually, so the text is redundant.
     */
    @Inject(
        method = "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T extends TooltipProvider> void suppressShulkerContainerTooltip(
            DataComponentType<T> type,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> consumer,
            TooltipFlag flag,
            CallbackInfo ci) {
        if (!ShulkerBoxUtilsConfig.isContentsPreviewEnabled()) return;
        if (type != DataComponents.CONTAINER) return;
        ItemStack self = (ItemStack) (Object) this;
        if (!(self.getItem() instanceof BlockItem bi)) return;
        if (!(bi.getBlock() instanceof ShulkerBoxBlock)) return;
        ci.cancel();
    }
}
