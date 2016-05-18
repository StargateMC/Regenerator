/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stargatemc.SRegen.integration;

import com.stargatemc.SRegen.config.integrationConfigHandler;
import com.jcdesimp.landlord.Landlord;
import com.jcdesimp.landlord.landMap.LandMap;
import com.jcdesimp.landlord.landMap.MapManager;
import com.jcdesimp.landlord.persistantData.OwnedLand;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public class LandlordIntegration extends Integration {

    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        OwnedLand land = OwnedLand.getLandFromDatabase(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        return land != null;
    }

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk) && getOwnerOfChunk(chunk) == player.getUniqueId() && player.hasPermission(getPermissionRequiredToRegen(player,chunk)) || player.hasPermission("SRegen.regen.landlord.OVERRIDE")) {
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
        integrationConfigHandler iConfig = new integrationConfigHandler(SRegenPlugin, this);
        iConfig.saveDefaultIntegrationConfig();
        if (!iConfig.integrationConfig.isSet("notice")) {
            iConfig.integrationConfig.set("notice", "Landlord integration does not utilize a config file.");
        }
        iConfig.saveIntegrationConfig();
    }
    
    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (canPlayerRegen(player,chunk)) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in Landlord protected territory.");
        } else {
            return (ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as you do not have the " + getPermissionRequiredToRegen(player,chunk) + " permission nodes");
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (player.getUniqueId() == getOwnerOfChunk(chunk)) {
            return "SRegen.regen.landlord.OWNER";
        } else {
            return "SRegen.regen.landlord.OVERRIDE";
        }
    }
    public UUID getOwnerOfChunk(Chunk chunk) {
        OwnedLand land = OwnedLand.getLandFromDatabase(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        if (isChunkClaimed(chunk)) {
            return land.getOwnerUUID();
        } else {
            return null;
        }
    }
    
    
}
