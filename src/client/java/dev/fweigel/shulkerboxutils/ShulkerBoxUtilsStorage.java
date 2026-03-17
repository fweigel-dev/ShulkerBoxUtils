package dev.fweigel.shulkerboxutils;

import dev.fweigel.mobutils.core.client.storage.WorldScopedStorage;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShulkerBoxUtilsStorage {

    private static final WorldScopedStorage<SaveData> STORAGE =
            new WorldScopedStorage<>("shulkerboxutils", SaveData.class,
                    LoggerFactory.getLogger(ShulkerBoxUtilsStorage.class));

    private static class SaveData {
        Map<String, String> items;
        List<String> nonUniformPositions;
        Boolean badgeEnabled;
        Boolean worldIconEnabled;
        Boolean uniformOnly;
        Boolean fillIndicatorEnabled;
        Boolean contentsPreviewEnabled;
    }

    private ShulkerBoxUtilsStorage() {}

    public static void loadForWorld(Minecraft client) {
        ShulkerBoxUtilsCache.ITEMS.clear();
        ShulkerBoxUtilsCache.IS_UNIFORM.clear();
        STORAGE.loadForWorld(client).ifPresentOrElse(data -> {
            if (data.items != null) {
                for (var entry : data.items.entrySet()) {
                    BlockPos pos = parsePos(entry.getKey());
                    ItemStack stack = parseItem(entry.getValue());
                    if (pos != null && stack != null && !stack.isEmpty()) {
                        ShulkerBoxUtilsCache.ITEMS.put(pos, stack);
                        boolean uniform = data.nonUniformPositions == null
                                || !data.nonUniformPositions.contains(entry.getKey());
                        ShulkerBoxUtilsCache.IS_UNIFORM.put(pos, uniform);
                    }
                }
            }
            ShulkerBoxUtilsConfig.setBadgeEnabled(data.badgeEnabled == null || data.badgeEnabled);
            ShulkerBoxUtilsConfig.setWorldIconEnabled(data.worldIconEnabled == null || data.worldIconEnabled);
            ShulkerBoxUtilsConfig.setUniformOnly(Boolean.TRUE.equals(data.uniformOnly));
            ShulkerBoxUtilsConfig.setFillIndicatorEnabled(data.fillIndicatorEnabled == null || data.fillIndicatorEnabled);
            ShulkerBoxUtilsConfig.setContentsPreviewEnabled(data.contentsPreviewEnabled == null || data.contentsPreviewEnabled);
        }, ShulkerBoxUtilsConfig::reset);
    }

    public static void save() {
        SaveData data = new SaveData();
        data.items = new HashMap<>();
        data.nonUniformPositions = new ArrayList<>();
        for (var entry : ShulkerBoxUtilsCache.ITEMS.entrySet()) {
            String posKey = posToString(entry.getKey());
            data.items.put(posKey, itemToString(entry.getValue()));
            Boolean uniform = ShulkerBoxUtilsCache.IS_UNIFORM.get(entry.getKey());
            if (Boolean.FALSE.equals(uniform)) {
                data.nonUniformPositions.add(posKey);
            }
        }
        data.badgeEnabled = ShulkerBoxUtilsConfig.isBadgeEnabled();
        data.worldIconEnabled = ShulkerBoxUtilsConfig.isWorldIconEnabled();
        data.uniformOnly = ShulkerBoxUtilsConfig.isUniformOnly();
        data.fillIndicatorEnabled = ShulkerBoxUtilsConfig.isFillIndicatorEnabled();
        data.contentsPreviewEnabled = ShulkerBoxUtilsConfig.isContentsPreviewEnabled();
        STORAGE.save(data);
    }

    private static String posToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static BlockPos parsePos(String s) {
        try {
            String[] p = s.split(",");
            return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }

    private static String itemToString(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static ItemStack parseItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
            if (item == null || item == Items.AIR) return null;
            return new ItemStack(item);
        } catch (Exception e) { return null; }
    }
}
