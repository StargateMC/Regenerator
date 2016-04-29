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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        this.chunkConfig = getChunkConfig();
    }
    
    public FileConfiguration getChunkConfig() {
        if (chunkConfig == null) {
            reloadChunkConfig();
        }
        return chunkConfig;
    }

    public void configureChunk () throws UnsupportedEncodingException {
        if (!chunkConfig.isSet("unloaded")) {
            chunkConfig.set("unloaded", 0);
        }
        if (!chunkConfig.isSet("loaded")) {
            chunkConfig.set("loaded", 0);
        }
        if (!chunkConfig.isSet("lastPlaced")) {
            chunkConfig.set("lastPlaced", 0);
        }
        if (!chunkConfig.isSet("lastBroken")) {
            chunkConfig.set("lastPlaced", 0);
        }
        if (!chunkConfig.isSet("lastRegen")) {
            chunkConfig.set("lastPlaced", 0);
        }
        if (!chunkConfig.isSet("lastClaimed")) {
            chunkConfig.set("lastClaimed", 0);
        }
        if (!chunkConfig.isSet("lastUnclaimed")) {
            chunkConfig.set("lastUnclaimed", 0);
        }
        if (!chunkConfig.isSet("autoregen")) {
            chunkConfig.set("autoregen", true);
        }
        if (!chunkConfig.isSet("manualregen")) {
            chunkConfig.set("manualregen", true);
        }
        // Saves the config file.
        saveChunkConfig();
    }
    
    public boolean getManualRegen() {
        this.reloadChunkConfig();
        return chunkConfig.getBoolean("manualregen");
    }
    public boolean getAutoRegen() {
        this.reloadChunkConfig();
        return chunkConfig.getBoolean("autoregen");
    }
    
    public long getLastRegen() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("lastRegen");
    }
    public long getLastBroken() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("lastBroken");
    }
    public long getLastPlaced() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("lastPlaced");
    }
    public long getLastLoaded() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("loaded");
    }
    public long getLastUnloaded() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("unloaded");
    }
    public long getLastClaimed() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("lastClaimed");
    }
    public void updateLastClaimed() {
        this.chunkConfig.set("lastClaimed", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public long getLastUnclaimed() {
        this.reloadChunkConfig();
        return this.chunkConfig.getLong("lastUnclaimed");
    }
    public void updateLastUnclaimed() {
        this.chunkConfig.set("lastUnclaimed", System.currentTimeMillis());
        this.saveChunkConfig();
    }
    public void updateLastPlaced() {
        this.reloadChunkConfig();
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
        try {
            saveDefaultChunkConfig();
            if (chunkConfigFile == null) {
                chunkConfigFile = new File(plugin.getDataFolder() + "/worlds/" + chunk.getWorld().getName() + "/chunks/", chunk.getX()+","+chunk.getZ() + ".yml");
            }
            chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);
            if (chunkConfig == null) {
                configureChunk();
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(chunkConfigHandler.class.getName()).log(Level.SEVERE, null, ex);
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
