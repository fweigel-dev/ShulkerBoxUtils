package dev.fweigel.shulkerboxutils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxTooltipComponent implements ClientTooltipComponent {

    // The vanilla shulker box container texture (256×256).
    // ShulkerBoxScreen blits 176×167 from (0,0). The outer panel spans the full height;
    // rounded top corners are in the first few rows and rounded bottom corners in the last few.
    //
    // We composite three strips to get a compact panel with correct rounded corners on both
    // sides and no wasted title-area space:
    //   1. Top strip    (srcV=0,   h=H_TOP)     → rounded top corners
    //   2. Slot grid    (srcV=17,  h=54)         → 3×9 slots with left/right panel border
    //   3. Bottom strip (srcV=163, h=H_BOTTOM)   → rounded bottom corners (last 4px of panel)
    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");

    private static final int COLS        = 9;
    private static final int ROWS        = 3;
    private static final int SLOT_SIZE   = 18;
    private static final int WIDTH       = 176;   // full panel width
    private static final int H_TOP       = 7;     // matches the 7px left/right panel border width
    private static final int H_GRID      = ROWS * SLOT_SIZE; // 54
    private static final int H_BOTTOM    = 7;     // matches the 7px left/right panel border width
    // imageHeight = 167; bottom strip starts at 167 - H_BOTTOM = 160
    private static final int TEX_V_BOT   = 160;
    // Item positions: slots start at texture x=7, y=17. Item is 1px inside the slot border.
    // In our composite layout the grid starts at y=H_TOP, so itemY offset = H_TOP + 1.
    private static final int ITEM_LEFT   = 8;           // x=7 slot border + 1
    private static final int ITEM_TOP    = H_TOP + 1;   // 8
    private static final int BOTTOM_PAD  = 3;
    private static final int TEX_SIZE    = 256;

    private final NonNullList<ItemStack> items;

    public ShulkerBoxTooltipComponent(ShulkerBoxTooltipData data) {
        this.items = data.items();
    }

    @Override
    public int getWidth(Font font) {
        return WIDTH;
    }

    @Override
    public int getHeight(Font font) {
        return H_TOP + H_GRID + H_BOTTOM + BOTTOM_PAD;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        // 1. Top strip — rounded top corners from the actual panel top.
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y, 0f, 0f, WIDTH, H_TOP, TEX_SIZE, TEX_SIZE);

        // 2. Slot grid — 54px from srcV=17, full panel width (includes left/right borders).
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y + H_TOP, 0f, 17f, WIDTH, H_GRID, TEX_SIZE, TEX_SIZE);

        // 3. Bottom strip — rounded bottom corners from the actual panel bottom (y=163–166).
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y + H_TOP + H_GRID, 0f, (float) TEX_V_BOT, WIDTH, H_BOTTOM, TEX_SIZE, TEX_SIZE);

        // Render each item with count and enchantment glint, exactly as in the real GUI.
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            int col = i % COLS;
            int row = i / COLS;
            int itemX = x + ITEM_LEFT + col * SLOT_SIZE;
            int itemY = y + ITEM_TOP  + row * SLOT_SIZE;
            graphics.renderItem(stack, itemX, itemY);
            graphics.renderItemDecorations(font, stack, itemX, itemY);
        }
    }
}
