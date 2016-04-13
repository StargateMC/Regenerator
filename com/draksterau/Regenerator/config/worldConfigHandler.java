/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.config;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class worldConfigHandler {
    
    public FileConfiguration worldConfig = null;
    File worldConfigFile = null;
    World world = null;
    RegeneratorPlugin plugin = null;
    
    public worldConfigHandler(RegeneratorPlugin plugin, World world) {
        this.world = world;
        this.plugin = plugin;
        getWorldConfig();
    }
    
    public FileConfiguration getWorldConfig() {
        if (worldConfig == null) {
            reloadWorldConfig();
        }
        return worldConfig;
    }

    public int getChunkInterval() {
        return worldConfig.getInt("interval");
    }
    
    public boolean getAutoRegenEnabled() {
        return worldConfig.getBoolean("autoregen");
    }
    public void configureWorld () {
        getWorldConfig();
        if (!worldConfig.isSet("interval")) {
            worldConfig.set("interval", 3600);
        }
        plugin.throwMessage("info", "Interval set to: " + getChunkInterval());
        
        if (!worldConfig.isSet("autoregen")) {
            worldConfig.set("autoregen", false);
        }
        plugin.throwMessage("info", "Automatic Regeneration set to: " + worldConfig.getBoolean("autoregen"));
        
        if (!worldConfig.isSet("manualregen")) {
            worldConfig.set("manualregen", false);
        }
        plugin.throwMessage("info", "Manual Regeneration set to: " + worldConfig.getBoolean("manualregen"));

        if (!worldConfig.isSet("populate-chunks")) {
            worldConfig.set("populate-chunks", true);
        }
        plugin.throwMessage("info", "Populate Chunks set to: " + worldConfig.getBoolean("populate-chunks"));
        
        
        if (!worldConfig.isSet("lastAction")) {
            worldConfig.set("lastAction", 0);
        }
        if (worldConfig.getLong("lastAction") != 0) {
            plugin.throwMessage("info", "Last Action performed: " + (plugin.convertMsToSecond(System.currentTimeMillis(), worldConfig.getLong("lastAction")) / 60) + " minutes ago");
        } else {
            plugin.throwMessage("info", "Last Action performed: Never");
        }
        if (!(worldConfig.isSet("skip-radius"))) {
             worldConfig.set("skip-radius", 0);
        }
        plugin.throwMessage("info", "Skip-Radius: " + worldConfig.getInt("skip-radius"));
        // Saves the config file.
        saveWorldConfig();
    }
    
    public boolean shouldPopulate() {
        return worldConfig.getBoolean("populate-chunks");
    }
    public boolean shouldRandomize() {
        return worldConfig.getBoolean("randomize-structures-and-ores");
    }
    public boolean getManualRegen() {
        return worldConfig.getBoolean("manualregen");
    }
     
    public boolean getAutoRegen() {
        return worldConfig.getBoolean("autoregen");
    }
    
    public long getSkipRadius() {
        return worldConfig.getLong("skip-radius");
    }
    
    public void setSkipRadius(int number) {
        worldConfig.set("skip-radius", number);
        this.saveWorldConfig();
    }
    
    public long getInterval() {
        return worldConfig.getLong("interval");
    }
    
    public long getLastAction() {
        return worldConfig.getLong("lastAction");
    }
    public void updateLastAction() {
        worldConfig.set("lastAction", System.currentTimeMillis());
        this.saveWorldConfig();
    }
    public void reloadWorldConfig() {
        saveDefaultWorldConfig();
        if (worldConfigFile == null) {
            worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + "/world.yml");
        }
        worldConfig = YamlConfiguration.loadConfiguration(worldConfigFile);

        if (worldConfig == null) {
            configureWorld();
        }
    }
    
    public void saveWorldConfig() {
        if (worldConfig == null || worldConfigFile == null) {
            return;
        }
        try {
            getWorldConfig().save(worldConfigFile);
        } catch (IOException ex) {
            plugin.throwMessage("severe","Could not save config to " + worldConfigFile + " (Exception: " + ex.getMessage() + ")");
        }
        worldConfig = null;
        worldConfigFile = null;
    }
    public void saveDefaultWorldConfig() {
        if (worldConfigFile == null) {
            worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + "/world.yml");
        }
        if (!worldConfigFile.exists()) {      
            worldConfig = YamlConfiguration.loadConfiguration(plugin.getResource("world.yml"));
            saveWorldConfig();
         }
    }
}
