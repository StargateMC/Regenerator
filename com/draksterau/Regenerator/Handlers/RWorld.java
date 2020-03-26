/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import org.bukkit.World;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
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
    public long regenInterval = 3600;
    public long minBlockAutoRegen = 0;
    public long maxBlockAutoRegen = 0;
    
    private File worldConfigFile;
    private FileConfiguration worldConfig;
    
    public RWorld(RegeneratorPlugin Regenerator, World world) {
        super(Regenerator);
        this.world = world;
        this.regenInterval = plugin.config.defaultRegenInterval;
        this.autoRegen = plugin.config.defaultAutoRegen;
        this.manualRegen = plugin.config.defaultManualRegen;
        this.loadData();
        this.validateWorld();
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
    public List<RChunk> getAllRChunks () {
        this.loadData();
        List<RChunk> allChunks = new ArrayList<RChunk>();
        allChunks.clear();
        File chunkConfigFile = null;
        FileConfiguration chunkConfig = null;
        // Attempt to load the config file.
        chunkConfigFile = new File(plugin.getDataFolder() + "/data/" + world.getName() + ".yml");
        // Attempt to read the config in the config file.
        chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);
        if (chunkConfig == null) return allChunks;
        if (!chunkConfig.isConfigurationSection("chunks")) return allChunks;
        for (Object chunk : chunkConfig.getConfigurationSection("chunks").getKeys(true).toArray()) {
            int x = Integer.valueOf(chunk.toString().split(",")[0]);
            int z = Integer.valueOf(chunk.toString().split(",")[1]);
            String worldName = this.world.getName();
            RChunk RChunk = new RChunk(plugin,x,z,worldName);
            allChunks.add(RChunk);
        }
        return allChunks;
    }
    
    public String getFormattedInterval() {
        String message = "";
        if (this.getIntervalDays() > 0) message += (this.getIntervalDays() + "d, ");
        if (this.getIntervalHours() > 0) message += ((this.getIntervalHours() - (this.getIntervalDays()*24)) + "h, ");
        if (this.getIntervalMins() > 0) message += ((this.getIntervalMins() - (this.getIntervalHours()*60)) + " m, ");
        if (this.getIntervalSecs() > 0) message += ((this.getIntervalSecs() - (this.getIntervalMins()*60)) + "s.");
        if (message.length() == 0) message = ChatColor.RED + "Disabled" + ChatColor.GRAY;
        return message;
    }
    @Override
    public void loadData() {
        // Attempt to load the config file.
        if (worldConfigFile == null) worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + ".yml");
        // Attempt to read the config in the config file.
        worldConfig = YamlConfiguration.loadConfiguration(worldConfigFile);
        // If the file doesnt exist, populate it from the template.
        if (!worldConfigFile.exists()) {
            worldConfigFile = new File(plugin.getDataFolder() + "/worlds/" + world.getName() + ".yml");
            worldConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("world.yml")));
            if (plugin.config.defaultAutoRegen) worldConfig.set("autoRegen", true);
            if (plugin.config.defaultManualRegen) worldConfig.set("manualRegen", true);
            saveData();
        }
        this.autoRegen = worldConfig.getBoolean("autoRegen");
        this.manualRegen = worldConfig.getBoolean("manualRegen");
        this.maxBlockAutoRegen = worldConfig.getLong("maxBlockAutoRegen");
        this.minBlockAutoRegen = worldConfig.getLong("minBlockAutoRegen");
        this.regenInterval = worldConfig.getLong("regenInterval");
    }

    public void validateWorld() {
        if (regenInterval < plugin.config.parseInterval) {
            this.plugin.utils.throwMessage(MsgType.WARNING, String.format(plugin.lang.getForKey("messages.regenIntervalTooFrequent"), this.world.getName()));
            this.plugin.utils.throwMessage(MsgType.WARNING, String.format(plugin.lang.getForKey("messages.regenIntervalTooFrequentSuggestion")));
        }
        if (regenInterval > (86400 * 30)) {
            this.plugin.utils.throwMessage(MsgType.WARNING, String.format(plugin.lang.getForKey("messages.regenIntervalAboveMax"), this.world.getName(), "30"));
            this.regenInterval = 86400*30;
        }
        if (regenInterval < 60) {
            this.plugin.utils.throwMessage(MsgType.WARNING, String.format(plugin.lang.getForKey("messages.regenIntervalBelowMin"), this.world.getName(), "1"));
            this.regenInterval = 60;
        }
        this.saveData();
        this.loadData();
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
            plugin.utils.throwMessage(MsgType.SEVERE,String.format(plugin.lang.getForKey("messages.cantSaveWorldConfig"), configFile.getAbsolutePath(), ex.getMessage()));
        }
    }
}
