/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.config.integrationConfigHandler;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public class GriefPreventionIntegration extends Integration {
    
    
    @Override
    public boolean isChunkClaimed (Chunk chunk) {
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims(chunk.getX(), chunk.getZ())) {
            if (claim.getGreaterBoundaryCorner().getWorld().equals(chunk.getWorld())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (player.hasPermission("regenerator.regen.griefprevention.OWNER") && getOwnerOfChunk(chunk) == player || player.hasPermission("regenerator.regen.griefprevention.OVERRIDE")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldChunkAutoRegen(Chunk chunk) {
        if (!isChunkClaimed(chunk)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void validateConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        iConfig.saveDefaultIntegrationConfig();
        if (!iConfig.integrationConfig.isSet("notice")) {
            iConfig.integrationConfig.set("notice", "Grief Prevention integration does not utilize a config file.");
        }
        iConfig.saveIntegrationConfig();
    }

    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (canPlayerRegen(player,chunk)) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in GriefPrevention protected territory.");
        } else {
            return (ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as you do not have the " + getPermissionRequiredToRegen(player,chunk) + " permission nodes");
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (getOwnerOfChunk(chunk) == player) {
            return ("regenerator.regen.griefprevention.OWNER");
        } else {
            return ("regenerator.regen.griefprevention.OVERRIDE");
        }
    }
    
    
    public static Player getOwnerOfChunk(Chunk chunk) {
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims(chunk.getX(), chunk.getZ())) {
            if (claim.getGreaterBoundaryCorner().getWorld().equals(chunk.getWorld())) {
                if (isOnlyOwnerOfChunk(chunk, Bukkit.getPlayer(claim.ownerID))) {
                    return Bukkit.getPlayer(claim.ownerID);
                }
            }
        }
        return null;
    }
    
    public static boolean isOnlyOwnerOfChunk(Chunk chunk, Player player) {
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims(chunk.getX(), chunk.getZ())) {
            if (claim.getGreaterBoundaryCorner().getWorld().equals(chunk.getWorld())) {
                if (Bukkit.getPlayer(claim.ownerID) != player) {
                    return false;
                }
            }
        }
        return true;
    }
}
