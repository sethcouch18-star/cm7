package com.example.colormons;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/** The paint overlay: the species texture as a pixel grid you can click. */
public class PaintPanel {
    public static final int CELL = 4;
    private static NativeImage source;
    private static String loadedFor;

    public static void load(String species) {
        if (source != null && species.equals(loadedFor)) return;
        reset();
        Optional<Resource> res = Minecraft.getInstance().getResourceManager()
                .getResource(GroupMaps.imageLocation(species));
        if (res.isEmpty()) return;
        try (InputStream in = res.get().open()) {
            source = NativeImage.read(in);
            loadedFor = species;
        } catch (Exception ignored) {}
    }

    public static void reset() {
        if (source != null) { source.close(); source = null; }
        loadedFor = null;
    }

    public static boolean ready() { return source != null; }
    public static int texWidth() { return source == null ? 0 : source.getWidth(); }
    public static int texHeight() { return source == null ? 0 : source.getHeight(); }

    public static void draw(GuiGraphics g, MonColors mc, int[] group, int originX, int originY) {
        if (source == null) return;
        int texW = source.getWidth(), texH = source.getHeight();
        int cols = Math.max(1, Math.min(texW, 160 / CELL));
        int rows = Math.max(1, Math.min(texH, 160 / CELL));
        Map<Integer, Integer> edits = EditBrush.asMap(mc);

        for (int cy = 0; cy < rows; cy++) for (int cx = 0; cx < cols; cx++) {
            int px = cx * texW / cols;
            int py = cy * texH / rows;
            int idx = py * texW + px;
            int argb = source.getPixelRGBA(px, py);
            if (((argb >>> 24) & 0xFF) == 0) continue;
            int rgb = ((argb & 0xFF) << 16) | (((argb >> 8) & 0xFF) << 8) | ((argb >> 16) & 0xFF);
            Integer edit = edits.get(idx);
            int grp = edit != null ? edit : (group != null && idx < group.length ? group[idx] : -1);
            if (grp >= 0 && grp < Palette.HIGHLIGHT.length) rgb = Palette.HIGHLIGHT[grp];
            g.fill(originX + cx * CELL, originY + cy * CELL,
                   originX + cx * CELL + CELL, originY + cy * CELL + CELL,
                   0xFF000000 | rgb);
        }
    }

    /** Mouse position -> texture pixel, or null when outside the grid. */
    public static int[] pixelAt(double mx, double my, int originX, int originY) {
        if (source == null) return null;
        int texW = source.getWidth(), texH = source.getHeight();
        int cols = Math.max(1, Math.min(texW, 160 / CELL));
        int rows = Math.max(1, Math.min(texH, 160 / CELL));
        int cx = (int) Math.floor((mx - originX) / CELL);
        int cy = (int) Math.floor((my - originY) / CELL);
        if (cx < 0 || cy < 0 || cx >= cols || cy >= rows) return null;
        return new int[]{cx * texW / cols, cy * texH / rows};
    }
}
