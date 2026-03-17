package dev.fweigel.shulkerboxutils;

import dev.fweigel.mobutils.core.client.util.ConfigKeyHelper;
import dev.fweigel.mobutils.core.client.util.ItemStackUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ShulkerBoxUtilsClient implements ClientModInitializer {

    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        configKey = ConfigKeyHelper.register("shulkerboxutils", "key.shulkerboxutils.config", GLFW.GLFW_KEY_B);

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ShulkerBoxTooltipData tooltipData) {
                return new ShulkerBoxTooltipComponent(tooltipData);
            }
            return null;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ShulkerBoxUtilsStorage.loadForWorld(client));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ShulkerBoxUtilsStorage.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                client.setScreen(new ShulkerBoxUtilsScreen());
            }
        });

        // Record which shulker box was clicked
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide()) {
                var be = level.getBlockEntity(hitResult.getBlockPos());
                if (be instanceof ShulkerBoxBlockEntity) {
                    ShulkerBoxUtilsCache.pendingPos = hitResult.getBlockPos().immutable();
                }
            }
            return InteractionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register((minecraft, screen, w, h) -> {
            if (!(screen instanceof ShulkerBoxScreen shulkerScreen)) return;

            final ItemStack[] lastFirst = { getFirstItem(shulkerScreen) };
            final boolean[] lastUniform = { isUniform(shulkerScreen) };

            if (!lastFirst[0].isEmpty()) {
                writeToCache(lastFirst[0], lastUniform[0]);
            }

            ScreenEvents.afterTick(screen).register(s -> {
                ItemStack currentFirst = getFirstItem(shulkerScreen);
                boolean currentUniform = isUniform(shulkerScreen);
                if (!ItemStack.isSameItem(currentFirst, lastFirst[0]) || currentUniform != lastUniform[0]) {
                    lastFirst[0] = currentFirst.copy();
                    lastUniform[0] = currentUniform;
                    writeToCache(lastFirst[0], lastUniform[0]);
                }
            });

            ScreenEvents.remove(screen).register(s -> {
                BlockPos pos = ShulkerBoxUtilsCache.pendingPos;
                if (pos != null) {
                    if (lastFirst[0].isEmpty()) {
                        ShulkerBoxUtilsCache.ITEMS.remove(pos);
                        ShulkerBoxUtilsCache.IS_UNIFORM.remove(pos);
                    } else {
                        ShulkerBoxUtilsCache.ITEMS.put(pos, lastFirst[0].copy());
                        ShulkerBoxUtilsCache.IS_UNIFORM.put(pos, lastUniform[0]);
                    }
                    ShulkerBoxUtilsCache.pendingPos = null;
                    ShulkerBoxUtilsStorage.save();
                }
            });
        });
    }

    private static ItemStack getFirstItem(ShulkerBoxScreen screen) {
        return screen.getMenu().slots.stream()
                .filter(s -> s.index < 27)
                .map(Slot::getItem)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    private static boolean isUniform(ShulkerBoxScreen screen) {
        List<ItemStack> slotItems = screen.getMenu().slots.stream()
                .filter(s -> s.index < 27)
                .map(Slot::getItem)
                .toList();
        // uniform = all non-empty items are the same type (or box is empty)
        return !ItemStackUtils.getUniformItem(slotItems).isEmpty() || getFirstItem(screen).isEmpty();
    }

    private static void writeToCache(ItemStack item, boolean uniform) {
        BlockPos pos = ShulkerBoxUtilsCache.pendingPos;
        if (pos == null) return;
        if (item.isEmpty()) {
            ShulkerBoxUtilsCache.ITEMS.remove(pos);
            ShulkerBoxUtilsCache.IS_UNIFORM.remove(pos);
        } else {
            ShulkerBoxUtilsCache.ITEMS.put(pos, item.copy());
            ShulkerBoxUtilsCache.IS_UNIFORM.put(pos, uniform);
        }
    }
}
