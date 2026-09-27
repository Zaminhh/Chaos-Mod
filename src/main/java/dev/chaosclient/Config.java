package dev.chaosclient;

import java.util.Set;

/** Compatibility facade used by mixins and feature code. */
public final class Config {
    private Config() {}
    public static void load() { ConfigManager.load(); }
    public static boolean enabled(Module module) { return ConfigManager.enabled(module); }
    public static void toggle(Module module) { ConfigManager.toggle(module); }
    public static void save() { ConfigManager.flushAsync(); }
    public static Set<Module> enabledModules() { return ConfigManager.enabledModulesSnapshot(); }
}
