/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RConfig extends RObject {

    // Configuration Version?
    public String configVersion = plugin.getDescription().getVersion();

    public RegeneratorPlugin getPlugin() {
        return plugin;
    }
    // Interval between task parses on worlds.
    public long parseInterval = 15;
    
    // How much of the interval in percent can be used for processing?
    public double percentIntervalRuntime = 0.1;
    
    // Maximum chunks per parse
    public long numChunksPerParse = 5;
    
    // Whether or not new worlds that are loaded should have manual regen enabled by default
    public boolean defaultManualRegen = false;
    
    // Whether or not new worlds that are loaded should have auto regen enabled by default
    public boolean defaultAutoRegen = false;
    
    // Minimum TPS for regenerator to continue running parses.
    public int minTpsRegen = 15;
    
    // Should Regenerator run without grief prevention plugins enabled?
    public boolean noGriefRun = true;
    
    public RConfig(RegeneratorPlugin plugin) {
        super(plugin);
        this.loadData();
    }

    public void validate() {
        if (minTpsRegen > 20 || minTpsRegen < 1) minTpsRegen = 15;
        
    }
    @Override
    void loadData() {
        // Attempt to load the config file.
        if (configFile == null) configFile = new File(plugin.getDataFolder() + "/global.yml");
        // Attempt to read the config in the config file.
        config = YamlConfiguration.loadConfiguration(configFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        if (configFile == null) configFile = new File(plugin.getDataFolder() + "/global.yml");
        // If the file doesnt exist, populate it from the template.
        if (!configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(plugin.getResource("global.yml"));
            saveData();
        } 
        this.defaultManualRegen = config.getBoolean("defaultManualRegen");
        this.defaultAutoRegen = config.getBoolean("defaultAutoRegen");
        this.noGriefRun = config.getBoolean("noGriefRun");
        this.minTpsRegen = config.getInt("minTpsRegen");
        this.configVersion = config.getString("configVersion");   
        this.parseInterval = config.getInt("parseInterval");
        this.numChunksPerParse =config.getInt("numChunksPerParse");
        this.percentIntervalRuntime = config.getDouble("percentIntervalRuntime");
    }

    @Override
    void saveData() {
        config.set("defaultManualRegen", this.defaultManualRegen);
        config.set("defaultAutoRegen", this.defaultAutoRegen);
        config.set("noGriefRun", this.noGriefRun);
        config.set("minTpsRegen", this.minTpsRegen);
        config.set("configVersion", this.configVersion);
        config.set("numChunksPerParse", this.numChunksPerParse);
        config.set("parseInterval", this.parseInterval);
        config.set("percentIntervalRuntime", this.percentIntervalRuntime);
        try {
            config.save(configFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage("severe","Could not save global config to " + configFile + " (Exception: " + ex.getMessage() + ")");
        }    }
    
}
