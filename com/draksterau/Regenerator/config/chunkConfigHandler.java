/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.config;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class chunkConfigHandler {
    
    public FileConfiguration chunkConfig = null;
    File chunkConfigFile = null;
    Chunk chunk = null;
    RegeneratorPlugin plugin = null;
    
    public chunkConfigHandler(RegeneratorPlugin plugin, Chunk chunk) {
        this.chunk = chunk;
        this.plugin = plugin;
        getChunkConfig();
    }
    
    public FileConfiguration getChunkConfig() {
        if (chunkConfig == null) {
            reloadChunkConfig();
        }
        return chunkConfig;
    }

    public void configureChunk () throws UnsupportedEncodingException {
        chunkConfig.set("unloaded", 0);
        chunkConfig.set("loaded", 0);
        chunkConfig.set("lastPlaced", 0);
        chunkConfig.set("lastBroken", 0);
        chunkConfig.set("lastRegen", 0);
        chunkConfig.set("autoregen", true);
        chunkConfig.set("manualregen", true);
        // Saves the config file.
        saveChunkConfig();
    }
    
    public boolean getManualRegen() {
        return chunkConfig.getBoolean("manualregen");
    }
    public boolean getAutoRegen() {
        return chunkConfig.getBoolean("autoregen");
    }
    
    public long getLastRegen() {
        return this.chunkConfig.getLong("lastRegen");
    }
    public long getLastBroken() {
        return this.chunkConfig.getLong("lastBroken");
    }
    public long getLastPlaced() {
        return this.chunkConfig.getLong("lastPlaced");
    }
    public long getLastLoaded() {
        return this.chunkConfig.getLong("loaded");
    }
    public long getLastUnloaded() {
        return this.chunkConfig.getLong("unloaded");
    }
    
    public void updateLastPlaced() {
        this.chunkConfig.set("lastPlaced", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public void updateLastBroken() {
        this.chunkConfig.set("lastBroken", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public void updateLastUnloaded() {
        this.chunkConfig.set("unloaded", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public void updateLastLoaded() {
        this.chunkConfig.set("loaded", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public void updateLastRegen() {
        this.chunkConfig.set("lastRegen", System.currentTimeMillis());
        worldConfigHandler wConfig = new worldConfigHandler(plugin, this.chunk.getWorld());
        wConfig.updateLastAction();
        this.saveChunkConfig();
    }
    public void reloadChunkConfig() {
        saveDefaultChunkConfig();
        if (chunkConfigFile == null) {
        chunkConfigFile = new File(plugin.getDataFolder() + "/worlds/" + chunk.getWorld().getName() + "/chunks/", chunk.getX()+","+chunk.getZ() + ".yml");
        }
        chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);

        if (chunkConfig == null) {
            try {
                configureChunk();
            } catch (UnsupportedEncodingException ex) {
                plugin.throwMessage("severe","Could not save config to " + chunkConfigFile + " (Exception: " + ex.getMessage() + ")");
            }
        }
    }
    
    public void saveChunkConfig() {
        if (chunkConfig == null || chunkConfigFile == null) {
            return;
        }
        try {
            getChunkConfig().save(chunkConfigFile);
        } catch (IOException ex) {
            plugin.throwMessage("severe","Could not save config to " + chunkConfigFile + " (Exception: " + ex.getMessage() + ")");
        }
        chunkConfig = null;
        chunkConfigFile = null;
    }
    public void saveDefaultChunkConfig() {
        if (chunkConfigFile == null) {
            chunkConfigFile = new File(plugin.getDataFolder() + "/worlds/" + chunk.getWorld().getName() + "/chunks/", chunk.getX()+","+chunk.getZ() + ".yml");
        }
        if (!chunkConfigFile.exists()) {      
            chunkConfig = YamlConfiguration.loadConfiguration(plugin.getResource("chunk.yml"));
            saveChunkConfig();
         }
    }
}
