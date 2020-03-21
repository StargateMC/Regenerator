/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RChunk extends RObject {
    
    public int chunkX;
    public int chunkZ;
    public String worldName;
    
    // Last activity time (in ms).
    // Default is -1, because a chunk with its activity reset will be 0. We need to be able to target chunks without their value modified to support regen on initial chunk load.
    public long lastActivity = -1;
    
    private File chunkConfigFile;
    private FileConfiguration chunkConfig;
    
    public RChunk(RegeneratorPlugin Regenerator, int chunkX, int chunkZ, String worldName) {
        super(Regenerator);
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldName = worldName;
        this.loadData();
    }
    
    public World getWorld() {
        return Bukkit.getServer().getWorld(worldName);
    }
    
    public Chunk getChunk() {
        return this.getWorld().getChunkAt(chunkX, chunkZ);
    }
    public boolean canAutoRegen() {
        RWorld world = new RWorld(this.plugin, this.getWorld());
        boolean isInactive = plugin.utils.validateChunkInactivity(this);
        boolean isWorldAllowing = world.canAutoRegen();
        if (isInactive && isWorldAllowing) return true;
        return false;
    }
    
    public boolean canManualRegen() {
        RWorld world = new RWorld(this.plugin, this.getWorld());
        return world.canManualRegen();
    }
    
    public String getWorldName() {
        return this.getWorld().getName();
    }
    
    public boolean isChunkLoaded() {
        return this.getChunk().isLoaded();
    }
    
    public void updateActivity() {
        Long oldLastActivity = this.lastActivity;
        this.lastActivity = System.currentTimeMillis();
        this.plugin.utils.throwMessage(MsgType.DEBUG, "Updating activity for chunk : " + this.chunkX + "," + this.chunkZ + " on world: " + worldName + " from : " + oldLastActivity + " to : " + lastActivity + ".");
        this.saveData();
    }
    public void resetActivity() {
        this.plugin.utils.throwMessage(MsgType.DEBUG, "Resetting activity for chunk : " + this.chunkX + "," + this.chunkZ + " on world: " + worldName + ", previous value : " + lastActivity);
        this.lastActivity = 0;
        this.saveData();
    }
    @Override
    void loadData() {
        // Attempt to load the config file.
        chunkConfigFile = new File(plugin.getDataFolder() + "/data/" + this.worldName + ".yml");
        // Attempt to read the config in the config file.
        chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        // If the file doesnt exist, populate it from the template.
        if (!chunkConfigFile.exists()) {
            chunkConfigFile = new File(plugin.getDataFolder() + "/data/" + this.worldName + ".yml");
            chunkConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("chunk.yml")));
            saveData();
        }
        if (chunkConfig.isSet("chunks." + chunkX + "," + chunkZ)) {
            this.lastActivity = chunkConfig.getLong("chunks." + chunkX + "," + chunkZ);
        }
    }

    @Override
    void saveData() {        
        chunkConfig = YamlConfiguration.loadConfiguration(chunkConfigFile);
        chunkConfig.set("chunks." + chunkX + "," + chunkZ, this.lastActivity);
        try {
            chunkConfig.save(chunkConfigFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage(MsgType.SEVERE,String.format(plugin.lang.getForKey("messages.chunkDataSaveFail"), chunkConfigFile.getAbsolutePath(), ex.getMessage()));
        }
    }

}
