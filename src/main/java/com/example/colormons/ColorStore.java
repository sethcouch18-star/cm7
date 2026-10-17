package com.example.colormons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ColorStore {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("colormons.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, MonColors>>(){}.getType();

    private static Map<String, MonColors> data = new HashMap<>();
    public static final int[][] SAVED = new int[4][MonColors.GROUPS];

    public static MonColors of(UUID id) {
        MonColors m = data.get(id.toString());
        if (m == null) { m = new MonColors(); data.put(id.toString(), m); }
        return m;
    }

    public static boolean has(UUID id) { return data.containsKey(id.toString()); }

    /** Store a palette that arrived from another player. Not written to disk. */
    public static void putRemote(UUID id, MonColors mc) {
        data.put(id.toString(), mc);
        TintCache.invalidate(id);
    }

    public static void touched(UUID id) {
        TintCache.invalidate(id);
        save();
        Sync.broadcast(id);
    }

    public static void clear(UUID id) {
        data.remove(id.toString());
        touched(id);
    }

    public static void save() {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("pokemon", data);
            root.put("saved", SAVED);
            Files.writeString(FILE, GSON.toJson(root));
        } catch (Exception ignored) {}
    }

    public static void load() {
        data = new HashMap<>();
        try {
            if (Files.exists(FILE)) {
                Map<String, Object> root = GSON.fromJson(Files.readString(FILE),
                        new TypeToken<Map<String, Object>>(){}.getType());
                if (root != null) {
                    Object mon = root.get("pokemon");
                    if (mon != null) {
                        Map<String, MonColors> parsed = GSON.fromJson(GSON.toJson(mon), MAP_TYPE);
                        if (parsed != null) data = parsed;
                    }
                    Object sv = root.get("saved");
                    if (sv != null) {
                        int[][] parsed = GSON.fromJson(GSON.toJson(sv), int[][].class);
                        if (parsed != null)
                            for (int i = 0; i < Math.min(SAVED.length, parsed.length); i++)
                                if (parsed[i] != null) SAVED[i] = parsed[i];
                    }
                }
            }
        } catch (Exception ignored) {}
        if (data == null) data = new HashMap<>();
    }
}
