/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Integrations;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */

public abstract class IntegrationRegistry {
    
    List<RIntegration> pluginsRegistered = new ArrayList<RIntegration>();
    
    public boolean registerIntegration(RIntegration r) {
        if (!r.isRequired() || !r.isSupported()) return false;
        if (pluginsRegistered.contains(r)) return false;
        pluginsRegistered.add(r);
        return pluginsRegistered.contains(r);
    }
    
    public boolean deregisterIntegration(RIntegration r) {
        if (!pluginsRegistered.contains(r)) return false;
        pluginsRegistered.remove(r);
        return pluginsRegistered.contains(r);
    }
    
    private List<RIntegration> getRegisteredIntegrations() {
        return this.pluginsRegistered;
    }
    
    public boolean isChunkClaimed(Chunk chunk) {
        for (RIntegration r : getRegisteredIntegrations()) {
            if (r.isChunkClaimed(chunk)) return true;
        }
        return false;
    }
    
    public boolean isChunkLocked(Chunk chunk) {
        int count = 0;
        for (RIntegration r : getRegisteredIntegrations()) {
            if (r.isChunkClaimed(chunk)) count++;
        }
        return (count > 1);
    }
    
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player, chunk))) return true; 
        return false;
    }
    
    public boolean shouldChunkAutoRegen(Chunk chunk) {
        if (isChunkClaimed(chunk)) return false;
        return true;
    }
    
    // This returns a success/failure message for a chunk that is formatted toward the plugin.
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getTerritoryAt(chunk) + ChatColor.GREEN + " territory.");
        } else {
            return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " in " + ChatColor.BLUE + getTerritoryAt(chunk) + ChatColor.RED + " territory without the " + ChatColor.GRAY + getPermissionRequiredToRegen(player, chunk) + ChatColor.RED + " permission node.";
        }
    }
    
    public String getTerritoryAt(Chunk chunk) {
        if (!this.isChunkClaimed(chunk)) return "unclaimed";
        if (this.isChunkLocked(chunk)) return "mixed";
        for (RIntegration r : this.getRegisteredIntegrations()) {
            if (r.isChunkClaimed(chunk)) return r.getTerritoryName(chunk);
        }
        return ChatColor.RED + "Error:" + ChatColor.GRAY + " Report this!";
    }
    
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (isChunkLocked(chunk)) return "regenerator.regen.override";
        if (!isChunkClaimed(chunk)) return "regenerator.regen.unclaimed";
        for (RIntegration r : getRegisteredIntegrations()) {
            if (r.isChunkClaimed(chunk)) return r.getPermissionRequiredToRegen(player, chunk);
        }
        return ChatColor.RED + "Error:" + ChatColor.GRAY + " Report this!";
    }
}
