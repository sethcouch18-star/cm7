package com.example.colormons;

import net.minecraft.client.gui.GuiGraphics;

/** Draws the group map of a species as flat colours, dimming everything but one group. */
public class GroupOverlay {

    public static void draw(GuiGraphics g, String species, int x, int y, int boxSize, int highlightGroup) {
        int[] map = GroupMaps.groups(species);
        if (map == null) return;
        GroupMaps.Size size = GroupMaps.sizeOf(species);
        if (size == null) return;

        int w = size.width(), h = size.height();
        int cell = Math.max(1, boxSize / Math.max(w, h));

        for (int cy = 0; cy < h; cy++) for (int cx = 0; cx < w; cx++) {
            int grp = map[cy * w + cx];
            if (grp < 0 || grp >= Palette.HIGHLIGHT.length) continue;
            int col = (highlightGroup >= 0 && grp != highlightGroup)
                    ? 0x22000000
                    : (0xFF000000 | Palette.HIGHLIGHT[grp]);
            g.fill(x + cx * cell, y + cy * cell, x + cx * cell + cell, y + cy * cell + cell, col);
        }
    }
}
