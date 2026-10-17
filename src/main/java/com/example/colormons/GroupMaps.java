package com.example.colormons;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Caches the group map and the texture size per species so the UI can draw them. */
public class GroupMaps {
    private static final Map<String, int[]> GROUPS = new HashMap<>();
    private static final Map<String, Size> SIZES = new HashMap<>();

    public record Size(int width, int height) {}

    public static int[] groups(String species) {
        int[] g = GROUPS.get(species);
        if (g != null) return g;
        NativeImage img = read(imageLocation(species));
        if (img == null) return null;
        NativeImage mask = read(TintCache.maskLocation(species));
        g = TextureAnalyzer.analyse(img, mask);
        if (mask != null) mask.close();
        SIZES.put(species, new Size(img.getWidth(), img.getHeight()));
        img.close();
        GROUPS.put(species, g);
        return g;
    }

    public static Size sizeOf(String species) {
        if (!SIZES.containsKey(species)) groups(species);
        return SIZES.get(species);
    }

    public static int count(String species, int wanted) {
        int[] g = groups(species);
        if (g == null) return 0;
        int n = 0;
        for (int v : g) if (v == wanted) n++;
        return n;
    }

    public static ResourceLocation imageLocation(String species) {
        return ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/pokemon/" + species + ".png");
    }

    private static NativeImage read(ResourceLocation loc) {
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(loc);
        if (res.isEmpty()) return null;
        try (InputStream in = res.get().open()) { return NativeImage.read(in); }
        catch (Exception e) { return null; }
    }
}
