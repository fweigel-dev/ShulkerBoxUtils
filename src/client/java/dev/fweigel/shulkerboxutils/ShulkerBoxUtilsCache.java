package dev.fweigel.shulkerboxutils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ShulkerBoxUtilsCache {

    /** First non-empty item in each placed shulker box. */
    public static final ConcurrentHashMap<BlockPos, ItemStack> ITEMS = new ConcurrentHashMap<>();

    /** True when all items in the box are the same type (or it's empty). */
    public static final ConcurrentHashMap<BlockPos, Boolean> IS_UNIFORM = new ConcurrentHashMap<>();

    /**
     * Positions whose cache values were set by an open screen event (authoritative).
     * The renderer mixin will NOT overwrite these with stale block-entity data.
     */
    public static final Set<BlockPos> SCREEN_AUTHORITATIVE =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Set when the player right-clicks a shulker box — cleared after screen closes
    public static BlockPos pendingPos = null;

    private ShulkerBoxUtilsCache() {}
}
