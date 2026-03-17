package dev.fweigel.shulkerboxutils.mixin;

import dev.fweigel.shulkerboxutils.ShulkerBoxTooltipData;
import dev.fweigel.shulkerboxutils.ShulkerBoxUtilsConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ShulkerItemTooltipMixin {

    @Inject(
        method = "getTooltipImage(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addContentsPreview(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (!ShulkerBoxUtilsConfig.isContentsPreviewEnabled()) return;
        if (!(stack.getItem() instanceof BlockItem bi)) return;
        if (!(bi.getBlock() instanceof ShulkerBoxBlock)) return;

        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(items);
        }
        cir.setReturnValue(Optional.of(new ShulkerBoxTooltipData(items)));
    }
}
