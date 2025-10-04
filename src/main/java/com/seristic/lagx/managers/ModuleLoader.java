package com.seristic.lagx.managers;

import com.seristic.lagx.api.Module;
import com.seristic.lagx.main.LagX;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * Handles loading and management of external modules
 * Responsible for discovering, loading, and managing plugin modules
 */
public class ModuleLoader {

    private final LagX plugin;
    private final Map<Module, String[]> loadedModules = new HashMap<>();
    private File moduleDirectory;

    public ModuleLoader(LagX plugin) {
        this.plugin = plugin;
        this.moduleDirectory = new File(plugin.getDataFolder(), "modules");
    }

    /**
     * Initialize module loading
     */
    public void initialize() {
        // Create module directory if it doesn't exist
        if (!moduleDirectory.exists()) {
            moduleDirectory.mkdirs();
        }

        // Set static reference for modules
        LagX.modDir = moduleDirectory;

        loadModules();
    }

    /**
     * Load all modules from the modules directory
     */
    public void loadModules() {
        loadedModules.clear();

        File[] moduleFiles = moduleDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (moduleFiles == null) {
            plugin.getLogger().info("No module files found in modules directory");
            return;
        }

        plugin.getLogger().info("Loading modules from " + moduleDirectory.getPath());

        for (File moduleFile : moduleFiles) {
            try {
                loadModule(moduleFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load module " + moduleFile.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + loadedModules.size() + " module(s)");
    }

    /**
     * Load a single module from a JAR file
     */
    private void loadModule(File moduleFile)
            throws IOException, InvalidConfigurationException, ReflectiveOperationException {
        try (ZipFile zipFile = new ZipFile(moduleFile)) {
            if (zipFile.getEntry("module.yml") == null) {
                plugin.getLogger().info(
                        "LagX located an invalid module named \"" + moduleFile.getName() + "\" - missing module.yml");
                return;
            }

            // Load module configuration
            YamlConfiguration moduleConfig = new YamlConfiguration();
            try (InputStreamReader reader = new InputStreamReader(
                    zipFile.getInputStream(zipFile.getEntry("module.yml")))) {
                moduleConfig.load(reader);
            }

            String name = moduleConfig.getString("name");
            String version = moduleConfig.getString("version");
            String author = moduleConfig.getString("author");
            String mainClass = moduleConfig.getString("main");

            if (name == null || version == null || author == null || mainClass == null) {
                plugin.getLogger().info("LagX located an invalid module named \"" + moduleFile.getName()
                        + "\" - incomplete module.yml");
                return;
            }

            // Load the module class
            try (URLClassLoader classLoader = new URLClassLoader(new URL[] { moduleFile.toURI().toURL() },
                    plugin.getClass().getClassLoader())) {
                Class<?> moduleClass = classLoader.loadClass(mainClass);
                Module module = (Module) moduleClass.getDeclaredConstructor().newInstance();

                // Store module information and enable it
                loadedModules.put(module, new String[] { name, version, author });
                module.onEnable();

                plugin.getLogger().info("Loaded module: " + name + " v" + version + " by " + author);
            }
        }
    }

    /**
     * Disable all loaded modules
     */
    public void shutdown() {
        for (Module module : loadedModules.keySet()) {
            try {
                module.onDisable();
            } catch (Exception e) {
                plugin.getLogger().warning("Error disabling module: " + e.getMessage());
            }
        }
        loadedModules.clear();
    }

    /**
     * Get information about loaded modules
     */
    public String[] getModulesList() {
        return loadedModules.values().stream()
                .map(info -> info[0] + " v" + info[1] + " by " + info[2])
                .toArray(String[]::new);
    }

    /**
     * Get data for a specific module
     */
    public String[] getData(Module module) {
        return loadedModules.get(module);
    }

    /**
     * Get the number of loaded modules
     */
    public int getLoadedModuleCount() {
        return loadedModules.size();
    }

    /**
     * Get all loaded modules
     */
    public Map<Module, String[]> getLoadedModules() {
        return new HashMap<>(loadedModules);
    }
}