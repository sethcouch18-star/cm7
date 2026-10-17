package com.example.colormons;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Works out which of the four groups each pixel belongs to.
 * 0 Primary, 1 Secondary, 2 Tertiary, 3 Custom (only ever set by the paint tool or a mask).
 */
public class TextureAnalyzer {
    public static final int GROUP_COUNT = 4;
    public static final int CUSTOM = 3;

    public static int[] analyse(NativeImage img, NativeImage mask) {
        int w = img.getWidth(), h = img.getHeight();
        int[] group = new int[w * h];
        Arrays.fill(group, -1);

        if (mask != null && mask.getWidth() == w && mask.getHeight() == h) {
            maskGroups(img, mask, group);
            return group;
        }

        kMeansBody(img, group);
        return group;
    }

    /** Red = Primary, green = Secondary, blue = Tertiary, yellow = Custom. */
    private static void maskGroups(NativeImage img, NativeImage mask, int[] group) {
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int m = mask.getPixelRGBA(x, y);
            int r = m & 0xFF, g = (m >> 8) & 0xFF, b = (m >> 16) & 0xFF, a = (m >>> 24) & 0xFF;
            if (a <= 0) continue;
            if (Math.max(r, Math.max(g, b)) <= 128) continue;
            int idx = y * w + x;
            if (r > 128 && g > 128 && b < 100) group[idx] = CUSTOM;
            else if (r >= g && r >= b) group[idx] = 0;
            else if (g >= b) group[idx] = 1;
            else group[idx] = 2;
        }
    }

    /** The three biggest colour clusters become Primary, Secondary and Tertiary. */
    private static void kMeansBody(NativeImage img, int[] group) {
        int w = img.getWidth(), h = img.getHeight();
        List<int[]> px = new ArrayList<>();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int p = img.getPixelRGBA(x, y);
            if (((p >>> 24) & 0xFF) == 0) continue;
            px.add(new int[]{x, y, p & 0xFF, (p >> 8) & 0xFF, (p >> 16) & 0xFF});
        }
        if (px.size() < 3) return;

        px.sort(Comparator.comparingDouble(q -> q[2] * 0.299 + q[3] * 0.587 + q[4] * 0.114));
        float[][] cen = new float[3][3];
        for (int k = 0; k < 3; k++) {
            int[] s = px.get(Math.min(px.size() - 1, px.size() * (2 * k + 1) / 6));
            cen[k] = new float[]{s[2], s[3], s[4]};
        }

        int[] assign = new int[px.size()];
        for (int it = 0; it < 10; it++) {
            float[][] sum = new float[3][3];
            int[] n = new int[3];
            for (int i = 0; i < px.size(); i++) {
                int[] q = px.get(i);
                int best = 0;
                float bd = Float.MAX_VALUE;
                for (int k = 0; k < 3; k++) {
                    float dr = q[2] - cen[k][0], dg = q[3] - cen[k][1], db = q[4] - cen[k][2];
                    float d = dr * dr + dg * dg + db * db;
                    if (d < bd) { bd = d; best = k; }
                }
                assign[i] = best;
                n[best]++;
                sum[best][0] += q[2]; sum[best][1] += q[3]; sum[best][2] += q[4];
            }
            for (int k = 0; k < 3; k++) {
                if (n[k] > 0) cen[k] = new float[]{sum[k][0] / n[k], sum[k][1] / n[k], sum[k][2] / n[k]};
            }
        }

        int[] count = new int[3];
        for (int a : assign) count[a]++;
        Integer[] order = {0, 1, 2};
        Arrays.sort(order, (a, b) -> count[b] - count[a]);
        for (int i = 0; i < px.size(); i++) {
            int[] q = px.get(i);
            group[q[1] * w + q[0]] = order[assign[i]];
        }
    }

    public static float lum(NativeImage img, int x, int y) {
        int p = img.getPixelRGBA(x, y);
        return ((p & 0xFF) * 0.299f + ((p >> 8) & 0xFF) * 0.587f + ((p >> 16) & 0xFF) * 0.114f) / 255f;
    }
}
