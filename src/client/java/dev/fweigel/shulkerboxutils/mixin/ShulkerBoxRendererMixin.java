package dev.fweigel.shulkerboxutils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.fweigel.mobutils.core.client.util.ItemStackUtils;
import dev.fweigel.shulkerboxutils.ShulkerBoxUtilsCache;
import dev.fweigel.shulkerboxutils.ShulkerBoxUtilsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxRenderer.class)
public class ShulkerBoxRendererMixin {

    @Unique
    private ItemModelResolver spResolver;

    @Inject(
        method = "<init>(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;)V",
        at = @At("TAIL")
    )
    private void captureResolver(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        this.spResolver = context.itemModelResolver();
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/level/block/entity/ShulkerBoxBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("TAIL")
    )
    private void cacheFirstItem(ShulkerBoxBlockEntity blockEntity, ShulkerBoxRenderState renderState,
                                float partialTick, Vec3 cameraPos,
                                ModelFeatureRenderer.CrumblingOverlay crumbling, CallbackInfo ci) {
        BlockPos pos = blockEntity.getBlockPos().immutable();
        boolean hasAny = hasAnyItem(blockEntity);

        if (hasAny) {
            ItemStack firstItem = getFirstItem(blockEntity);
            ShulkerBoxUtilsCache.ITEMS.put(pos, firstItem.copy());
            // Only seed IS_UNIFORM from the block entity if no authoritative value exists yet.
            // The client-side block entity inventory is stale for closed boxes (the server only
            // sends slot updates to the open container menu, not back to the block entity).
            // Screen events in ShulkerBoxUtilsClient are the authoritative source for updates.
            if (!ShulkerBoxUtilsCache.IS_UNIFORM.containsKey(pos)) {
                boolean uniform = !ItemStackUtils.getUniformItem(blockEntity).isEmpty();
                ShulkerBoxUtilsCache.IS_UNIFORM.put(pos, uniform);
            }
        }
        // Never clear the cache here: the client-side block entity appears empty for closed
        // boxes because inventory packets only go to the open container, not to the BE itself.
        // Screen events (ShulkerBoxUtilsClient) are responsible for clearing cache entries.
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("TAIL")
    )
    private void renderIcon(ShulkerBoxRenderState renderState, PoseStack poseStack,
                            SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
                            CallbackInfo ci) {
        if (!ShulkerBoxUtilsConfig.isWorldIconEnabled()) return;
        if (spResolver == null) return;

        ItemStack firstItem = ShulkerBoxUtilsCache.ITEMS.get(renderState.blockPos);
        if (firstItem == null || firstItem.isEmpty()) return;

        boolean uniform = ShulkerBoxUtilsCache.IS_UNIFORM.getOrDefault(renderState.blockPos, true);
        if (ShulkerBoxUtilsConfig.isUniformOnly() && !uniform) return;

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        ItemStackRenderState itemRenderState = new ItemStackRenderState();
        spResolver.updateForTopItem(itemRenderState, firstItem, ItemDisplayContext.FIXED, level, null, 0);
        if (itemRenderState.isEmpty()) return;

        Direction dir = renderState.direction != null ? renderState.direction : Direction.UP;

        poseStack.pushPose();
        applyFaceTransform(poseStack, dir, renderState.progress);
        itemRenderState.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Unique
    private static ItemStack getFirstItem(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private static boolean hasAnyItem(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    /**
     * Positions and rotates the item so it sits on the face the shulker lid opens toward,
     * oriented exactly like an item frame placed on that face, and moves with the lid as it opens.
     */
    @Unique
    private static void applyFaceTransform(PoseStack poseStack, Direction dir, float progress) {
        double o = 0.01; // tiny gap to avoid z-fighting
        double lid = progress * 0.5; // lid travels ~0.5 blocks at full open
        switch (dir) {
            case UP -> {
                poseStack.translate(0.5, 1.0 + lid + o, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case DOWN -> {
                poseStack.translate(0.5, -lid - o, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case NORTH -> {
                poseStack.translate(0.5, 0.5, -lid - o);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 1.0 + lid + o);
                // no rotation — item face already points toward Z+ (south)
            }
            case EAST -> {
                poseStack.translate(1.0 + lid + o, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case WEST -> {
                poseStack.translate(-lid - o, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
        }
        // Undo the 180°Y mirror baked into ItemDisplayContext.FIXED
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.scale(0.5f, 0.5f, 0.5f);
    }
}
