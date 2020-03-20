/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.RegeneratorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public abstract class Integration {
    
    public String plugin;
    public RegeneratorPlugin RegeneratorPlugin;
    
    public boolean isIntegrationRequired() {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
    
    // This gets the plugin name for this Integration.
    public String getPluginName() {
        return plugin;
    }
    
    // This obtains the plugin version.
    public String getPluginVersion() {
        return Bukkit.getPluginManager().getPlugin(plugin).getDescription().getVersion();
    }
    
    // This handles basic checks for if the chunk is claimed.
    public abstract boolean isChunkClaimed(Chunk chunk);
    
    // This handles if a player can regenerate or not based on Integration.
    public abstract boolean canPlayerRegen(Player player, Chunk chunk);
    
    // This handles if a chunk should be regenerated automatically.
    public abstract boolean shouldChunkAutoRegen(Chunk chunk);
    
    // This validates the configuration is suitable for the plugin.
    public abstract void validateConfig();
    
    // This returns a success/failure message for a chunk that is formatted toward the plugin.
    public abstract String getPlayerRegenReason(Player player, Chunk chunk);
    
    // This returns the permission required to regenerate a chunk, regardless of if the player has it.
    public abstract String getPermissionRequiredToRegen(Player player, Chunk chunk);
    
    // This returns true or false if the integration will conflict with unknown protection detection.
    public abstract boolean supportsUnknownProtectionDetection();
    
}
