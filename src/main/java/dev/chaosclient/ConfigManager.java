package dev.chaosclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("chaosclient.json");
    private static final Object LOCK = new Object();
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ChaosClient-ConfigWriter");
        thread.setDaemon(true);
        return thread;
    });
    private static final long SAVE_INTERVAL_MS = 30_000L;
    private static Data data = new Data();
    private static final EnumSet<Module> enabledModules = EnumSet.noneOf(Module.class);
    private static volatile boolean dirty;
    private static volatile boolean writeQueued;
    private static long revision;
    private static long lastSave = System.currentTimeMillis();

    private ConfigManager() {}

    public static void load() {
        synchronized (LOCK) {
            if (Files.isRegularFile(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                    Data loaded = GSON.fromJson(reader, Data.class);
                    if (loaded != null) data = loaded;
                } catch (IOException ignored) {}
            }
            normalize();
            rebuildEnabledSet();
        }
        markDirty();
    }

    private static void normalize() {
        if (data.enabled == null) data.enabled = new EnumMap<>(Module.class);
        if (data.expanded == null) data.expanded = new EnumMap<>(Module.class);
        if (data.numbers == null) data.numbers = new HashMap<>();
        if (data.booleans == null) data.booleans = new HashMap<>();
        if (data.modes == null) data.modes = new HashMap<>();
        if (data.keybinds == null) data.keybinds = new EnumMap<>(Module.class);
        if (data.colors == null) data.colors = new EnumMap<>(Module.class);
        for (Module module : Module.values()) {
            data.enabled.putIfAbsent(module, module.defaultEnabled);
            data.expanded.putIfAbsent(module, false);
            data.keybinds.putIfAbsent(module, -1);
            data.colors.putIfAbsent(module, defaultColor(module));
        }
        putNumber("auraReach", 4.5); putNumber("auraCps", 16.0); putNumber("auraTargets", 3.0);
        putNumber("reachDistance", 4.5); putNumber("flySpeed", 0.8); putNumber("speedMultiplier", 1.6);
        putBoolean("espPlayers", true); putBoolean("espMobs", true); putBoolean("espItems", true);
        putBoolean("espStorage", true); putBoolean("espSpawners", true); putBoolean("espSulfurCubes", true);
        data.modes.putIfAbsent("auraSort", "DISTANCE");
        data.modes.putIfAbsent("espBox", "OUTLINE");
    }

    private static void putNumber(String key, double value) { data.numbers.putIfAbsent(key, value); }
    private static void putBoolean(String key, boolean value) { data.booleans.putIfAbsent(key, value); }
    private static int defaultColor(Module module) {
        return switch (module) { case ESP -> 0xFF4DA6FF; case XRAY -> 0xFF00FFFF; default -> 0xFF7C4DFF; };
    }
    private static void rebuildEnabledSet() {
        enabledModules.clear();
        for (Module module : Module.values()) if (Boolean.TRUE.equals(data.enabled.get(module))) enabledModules.add(module);
    }

    public static boolean enabled(Module module) { synchronized (LOCK) { return enabledModules.contains(module); } }
    public static Set<Module> enabledModulesSnapshot() { synchronized (LOCK) { return EnumSet.copyOf(enabledModules); } }
    public static void toggle(Module module) { setEnabled(module, !enabled(module)); }
    public static void setEnabled(Module module, boolean value) {
        synchronized (LOCK) { data.enabled.put(module, value); if (value) enabledModules.add(module); else enabledModules.remove(module); }
        if (!value) FeatureController.onDisabled(module);
        markDirty();
    }
    public static boolean expanded(Module module) { synchronized (LOCK) { return data.expanded.getOrDefault(module, false); } }
    public static void setExpanded(Module module, boolean value) { synchronized (LOCK) { data.expanded.put(module, value); } markDirty(); }
    public static double number(String key) { synchronized (LOCK) { return data.numbers.getOrDefault(key, 0.0); } }
    public static void setNumber(String key, double value) { synchronized (LOCK) { data.numbers.put(key, value); } markDirty(); }
    public static boolean bool(String key) { synchronized (LOCK) { return data.booleans.getOrDefault(key, false); } }
    public static void setBool(String key, boolean value) { synchronized (LOCK) { data.booleans.put(key, value); } markDirty(); }
    public static String mode(String key) { synchronized (LOCK) { return data.modes.getOrDefault(key, ""); } }
    public static void setMode(String key, String value) { synchronized (LOCK) { data.modes.put(key, value); } markDirty(); }
    public static int keybind(Module module) { synchronized (LOCK) { return data.keybinds.getOrDefault(module, -1); } }
    public static void setKeybind(Module module, int key) { synchronized (LOCK) { data.keybinds.put(module, key); } markDirty(); }
    public static int color(Module module) { synchronized (LOCK) { return data.colors.getOrDefault(module, defaultColor(module)); } }
    public static void setColor(Module module, int color) { synchronized (LOCK) { data.colors.put(module, color); } markDirty(); }
    public static int guiX() { synchronized (LOCK) { return data.guiX; } }
    public static int guiY() { synchronized (LOCK) { return data.guiY; } }
    public static void setGuiPosition(int x, int y) { synchronized (LOCK) { data.guiX = x; data.guiY = y; } markDirty(); }
    public static String selectedCategory() { synchronized (LOCK) { return data.selectedCategory; } }
    public static void setSelectedCategory(String value) { synchronized (LOCK) { data.selectedCategory = value; } markDirty(); }

    public static void tick() { if (dirty && System.currentTimeMillis() - lastSave >= SAVE_INTERVAL_MS) flushAsync(); }
    public static void markDirty() { synchronized (LOCK) { revision++; dirty = true; } }
    public static void flushAsync() {
        if (!dirty || writeQueued) return;
        writeQueued = true;
        WRITER.execute(() -> {
            try {
                String json;
                long writingRevision;
                synchronized (LOCK) { json = GSON.toJson(data); writingRevision = revision; }
                Files.createDirectories(PATH.getParent());
                Path temporary = PATH.resolveSibling(PATH.getFileName() + ".tmp");
                Files.writeString(temporary, json, StandardCharsets.UTF_8);
                try { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
                catch (IOException unsupportedAtomicMove) { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING); }
                synchronized (LOCK) {
                    dirty = revision != writingRevision;
                    lastSave = System.currentTimeMillis();
                }
            } catch (IOException ignored) { dirty = true; }
            finally { writeQueued = false; }
        });
    }

    public static final class Data {
        public Map<Module, Boolean> enabled = new EnumMap<>(Module.class);
        public Map<Module, Boolean> expanded = new EnumMap<>(Module.class);
        public Map<String, Double> numbers = new HashMap<>();
        public Map<String, Boolean> booleans = new HashMap<>();
        public Map<String, String> modes = new HashMap<>();
        public Map<Module, Integer> keybinds = new EnumMap<>(Module.class);
        public Map<Module, Integer> colors = new EnumMap<>(Module.class);
        public int guiX = 24;
        public int guiY = 24;
        public String selectedCategory = "Render";
    }
}
