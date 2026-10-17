package com.example.colormons;

import com.mojang.blaze3d.platform.NativeImage;

/** Pixel tweaks for the Matte and Glossy finishes. */
public class Finish {
    public static final int DEFAULT = 0;
    public static final int GLOSSY = 1;
    public static final int MATTE = 2;
    public static final int SHINY = 3;

    public static void apply(NativeImage img, int x, int y, int p, int a, float l, int finish, float shade) {
        if (finish == MATTE) {
            float flat = 0.62f + 0.38f * clamp01(shade);
            img.setPixelRGBA(x, y, scale(p, a, flat));
        } else if (finish == GLOSSY) {
            float contrast = 1.25f * (shade - 0.5f) + 0.5f;
            float highlight = Math.max(0f, 1f - Math.abs(0.25f - l) * 3.2f) * 0.22f;
            img.setPixelRGBA(x, y, scale(p, a, Math.min(1.6f, contrast + highlight)));
        }
    }

    private static int scale(int p, int a, float f) {
        int r = clamp((p & 0xFF) * f);
        int g = clamp(((p >> 8) & 0xFF) * f);
        int b = clamp(((p >> 16) & 0xFF) * f);
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
    private static int clamp(float v) { return Math.max(0, Math.min(255, (int) v)); }
}
