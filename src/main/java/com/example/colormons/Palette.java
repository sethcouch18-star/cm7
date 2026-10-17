package com.example.colormons;

/** Group names, highlight colours and the fallback swatches the UI draws. */
public class Palette {
    public static final String[] GROUP_NAMES = {"Primary", "Secondary", "Tertiary", "Custom"};
    public static final int[] FALLBACK = {0xFFCC33, 0xCC3333, 0x2A2A2A, 0xBB66FF};
    /** Flat colours used when drawing the group map over the preview. */
    public static final int[] HIGHLIGHT = {0xFF4040, 0x40FF40, 0x4080FF, 0xFFD040};
    public static final String[] FINISH_NAMES = {"Default", "Glossy", "Matte", "Shiny"};

    public static int shown(MonColors mc, int group) {
        return mc.colors[group] >= 0 ? mc.colors[group] : FALLBACK[group];
    }
}
