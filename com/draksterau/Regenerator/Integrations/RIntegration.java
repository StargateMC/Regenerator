/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Integrations;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public abstract class RIntegration {
    
    // This is the plugin name for the Integrated plugin.
    public String pluginName;
    // Version String supported by Regenerator. This is usually Major.Minor version, with anything after that treated as a minor change unlikely to break things.
    public String supportedVersion;
    // SubClass Name, to be loaded by the Integration engine.
    public String subClassName;
    
    public boolean isRequired() {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
    
    public boolean isSupported() {
        return (isRequired() && (Bukkit.getPluginManager().getPlugin(pluginName)).getDescription().getVersion().startsWith(supportedVersion));
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public String getPluginVersion() {
        return Bukkit.getPluginManager().getPlugin(pluginName).getDescription().getVersion();
    }
    
    // This handles basic checks for if the chunk is claimed.
    public abstract boolean isChunkClaimed(Chunk chunk);
    
    // This handles if a player can regenerate or not based on RIntegration.
    public abstract boolean canPlayerRegen(Player player, Chunk chunk);
    
    // This handles if a chunk should be regenerated automatically.
    public abstract boolean shouldChunkAutoRegen(Chunk chunk);
    
    // This validates the configuration is suitable for the plugin.
    public abstract void validateConfig();
    
    // This returns a success/failure message for a chunk that is formatted toward the plugin.
    public abstract String getPlayerRegenReason(Player player, Chunk chunk);
    
    // This returns the permission required to regenerate a chunk, regardless of if the player has it.
    public abstract String getPermissionRequiredToRegen(Player player, Chunk chunk);
    
    // This returns the territory within a chunk. It should always return a value.
    public abstract String getTerritoryName(Chunk chunk);
    
}
