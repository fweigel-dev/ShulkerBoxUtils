package dev.fweigel.shulkerboxutils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

public final class ShulkerBoxUtilsCache {

    /** First non-empty item in each placed shulker box. */
    public static final ConcurrentHashMap<BlockPos, ItemStack> ITEMS = new ConcurrentHashMap<>();

    /** True when all items in the box are the same type (or it's empty). */
    public static final ConcurrentHashMap<BlockPos, Boolean> IS_UNIFORM = new ConcurrentHashMap<>();

    // Set when the player right-clicks a shulker box — cleared after screen closes
    public static BlockPos pendingPos = null;

    private ShulkerBoxUtilsCache() {}
}
