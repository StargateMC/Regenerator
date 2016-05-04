/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RChunk extends RObject {
    
    public Chunk chunk;
    
    // Last activity time (in ms).
    public long lastActivity = 0;
    
    private File chunkConfigFile;
    private FileConfiguration chunkConfig;
    
    public RChunk(RegeneratorPlugin Regenerator, Chunk chunk) {
        super(Regenerator);
        this.chunk = chunk;
        this.loadData();
    }
    
    public World getWorld() {
        return this.chunk.getWorld();
    }
    
    public boolean canAutoRegen() {
        RWorld world = new RWorld(this.plugin, this.chunk.getWorld());
        return world.canAutoRegen();
    }
    
    public boolean canManualRegen() {
        RWorld world = new RWorld(this.plugin, this.chunk.getWorld());
        return world.canManualRegen();
    }
    
    public String getWorldName() {
        return this.chunk.getWorld().getName();
    }

    @Override
    void loadData() {
        // Attempt to load the config file.
        if (chunkConfigFile == null) chunkConfigFile = new File(plugin.getDataFolder() + "/data/" + chunk.getWorld().getName() + ".yml");
        // Attempt to read the config in the config file.
        chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        if (chunkConfig == null) chunkConfigFile = new File(plugin.getDataFolder() + "/data/" + chunk.getWorld().getName() + ".yml");
        // If the file doesnt exist, populate it from the template.
        if (!chunkConfigFile.exists()) chunkConfig = YamlConfiguration.loadConfiguration(plugin.getResource("chunk.yml")); saveData();
        if (chunkConfig.isSet(chunk.getX() + "," + chunk.getZ())) {
            this.lastActivity = chunkConfig.getLong(chunk.getX() + "," + chunk.getZ());
        } else {
            saveData();
        }
    }

    @Override
    void saveData() {
        chunkConfig.set(chunk.getX() + "," + chunk.getZ(), this.lastActivity);
        try {
            chunkConfig.save(chunkConfigFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage("severe","Could not save chunk data to " + chunkConfigFile + " (Exception: " + ex.getMessage() + ")");
        }
    }

}
