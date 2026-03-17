package dev.fweigel.shulkerboxutils;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ShulkerBoxTooltipData(NonNullList<ItemStack> items) implements TooltipComponent {
}
