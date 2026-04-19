package com.goodboy.scoreboardswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class SwapConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("scoreboardswap.json");

    // originalText -> replacementText
    public static Map<String, String> replacements = new LinkedHashMap<>();

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            replacements = new LinkedHashMap<>();
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
            Map<String, String> loaded = GSON.fromJson(reader, type);
            replacements = loaded != null ? loaded : new LinkedHashMap<>();
        } catch (Exception e) {
            replacements = new LinkedHashMap<>();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(replacements, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Given a raw scoreboard string (may contain § color codes),
     * returns the replacement if the VISIBLE (stripped) text matches any key.
     * Colors/formatting codes on the original are preserved — only the visible text changes.
     */
    public static String applyReplacement(String original) {
        if (original == null) return null;
        String stripped = original.replaceAll("§[0-9a-fk-or]", "");
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (stripped.equals(entry.getKey())) {
                // Preserve any leading color/format codes before the text
                String leadingCodes = extractLeadingCodes(original);
                return leadingCodes + entry.getValue();
            }
        }
        return original;
    }

    private static String extractLeadingCodes(String s) {
        StringBuilder codes = new StringBuilder();
        int i = 0;
        while (i + 1 < s.length() && s.charAt(i) == '§') {
            codes.append(s, i, i + 2);
            i += 2;
        }
        return codes.toString();
    }
}
