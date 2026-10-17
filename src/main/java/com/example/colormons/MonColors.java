package com.example.colormons;

import java.util.ArrayList;
import java.util.List;

/** Saved palette for one Pokemon. */
public class MonColors {
    public static final int GROUPS = 4;
    /** -1 = leave this group alone. 0 Primary, 1 Secondary, 2 Tertiary, 3 Custom. */
    public int[] colors = {-1, -1, -1, -1};
    /** 0 Default, 1 Glossy, 2 Matte, 3 Shiny. */
    public int finish = 0;
    /** Pixel index -> group. Written by the paint tool, saved with the palette. */
    public List<Integer> editPixels = new ArrayList<>();
    public List<Integer> editGroups = new ArrayList<>();

    public boolean isEmpty() {
        if (finish == 1 || finish == 2) return false;
        for (int c : colors) if (c >= 0) return false;
        return editPixels.isEmpty();
    }

    public void reset() {
        colors = new int[]{-1, -1, -1, -1};
        finish = 0;
        editPixels = new ArrayList<>();
        editGroups = new ArrayList<>();
    }
}
