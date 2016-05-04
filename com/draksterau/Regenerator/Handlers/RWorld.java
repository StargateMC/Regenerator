/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.google.gson.Gson;
import org.bukkit.World;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RWorld extends RObject {
    
    public World world;
    public boolean autoRegen = false;
    public boolean manualRegen = false;
    public long regenInterval = 86400;
    public long minBlockAutoRegen = 0;
    public long maxBlockAutoRegen = 0;
    
    private File worldConfigFile;
    private FileConfiguration worldConfig;
    
    public RWorld(RegeneratorPlugin Regenerator, World world) {
        super(Regenerator);
        this.world = world;
        this.loadData();
    }
    public long getIntervalDays() {
        return (this.regenInterval / 86400);
    }
    public long getIntervalHours() {
        return (this.regenInterval / 3600);
    }
    public long getIntervalMins() {
        return (this.regenInterval / 60);
    }
    public long getIntervalSecs() {
        return (this.regenInterval);
    }
    public boolean canAutoRegen() {
        return this.autoRegen;
    }
    public boolean canManualRegen() {
        return this.manualRegen;
    }

    public long getNumChunks() {
        // Get the number of chunks available.
        return ((this.maxBlockAutoRegen - this.minBlockAutoRegen) / 16) ^ 2;
    }
    
    @Override
    void loadData() {
        // Attempt to load the config file.
        if (worldConfigFile == null) worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + ".yml");
        // Attempt to read the config in the config file.
        worldConfig = YamlConfiguration.loadConfiguration(worldConfigFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        if (worldConfig == null) worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + ".yml");
        // If the file doesnt exist, populate it from the template.
        if (!worldConfigFile.exists()) worldConfig = YamlConfiguration.loadConfiguration(plugin.getResource("world.yml")); saveData();
        this.autoRegen = worldConfig.getBoolean("autoRegen");
        this.manualRegen = worldConfig.getBoolean("manualRegen");
        this.maxBlockAutoRegen = worldConfig.getLong("maxBlockAutoRegen");
        this.minBlockAutoRegen = worldConfig.getLong("minBlockAutoRegen");
        this.regenInterval = worldConfig.getLong("regenInterval");        
    }

    @Override
    void saveData() {
        worldConfig.set("minBlockAutoRegen", this.minBlockAutoRegen);
        worldConfig.set("maxBlockAutoRegen", this.maxBlockAutoRegen);
        worldConfig.set("manualRegen", this.canManualRegen());
        worldConfig.set("regenInterval", this.getIntervalSecs());
        worldConfig.set("autoRegen", this.canAutoRegen());
        try {
            worldConfig.save(worldConfigFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage("severe","Could not save world config to " + worldConfigFile + " (Exception: " + ex.getMessage() + ")");
        }
    }
}
