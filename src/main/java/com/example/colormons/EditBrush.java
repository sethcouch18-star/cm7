package com.example.colormons;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.HashMap;
import java.util.Map;

/** Small brush that paints pixels of a species texture into the Custom group. */
public class EditBrush {
    public static int size = 1;

    /** Adds a filled circle of pixels around (cx, cy) to the Custom group. */
    public static void paint(MonColors mc, int cx, int cy, int width, int height) {
        for (int dy = -size; dy <= size; dy++) for (int dx = -size; dx <= size; dx++) {
            if (dx * dx + dy * dy > size * size + size) continue;
            int x = cx + dx, y = cy + dy;
            if (x < 0 || y < 0 || x >= width || y >= height) continue;
            set(mc, y * width + x, TextureAnalyzer.CUSTOM);
        }
    }

    public static void set(MonColors mc, int index, int group) {
        for (int i = 0; i < mc.editPixels.size(); i++) {
            if (mc.editPixels.get(i) == index) { mc.editGroups.set(i, group); return; }
        }
        mc.editPixels.add(index);
        mc.editGroups.add(group);
    }

    /** Undo the last N painted pixels. */
    public static void undo(MonColors mc, int n) {
        for (int i = 0; i < n && !mc.editPixels.isEmpty(); i++) {
            mc.editPixels.remove(mc.editPixels.size() - 1);
            mc.editGroups.remove(mc.editGroups.size() - 1);
        }
    }

    public static Map<Integer, Integer> asMap(MonColors mc) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < mc.editPixels.size(); i++) map.put(mc.editPixels.get(i), mc.editGroups.get(i));
        return map;
    }

    public static void readMap(MonColors mc, NativeImage img, int[] group) {}
}
