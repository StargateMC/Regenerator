/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public class TownyIntegration extends Integration {

    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        WorldCoord worldcoord = new WorldCoord(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        try {
            return worldcoord.getTownBlock().hasTown();
        } catch (NotRegisteredException ex) {
                return false;
        }
    }

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk)) {
            if (player.hasPermission("regenerator.regen.towny." + getRoleForChunk(chunk,player))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getRoleForChunk(Chunk chunk, Player player) {
        if (isChunkClaimed(chunk)) {
            Town town = getTownForChunk(chunk);
            List<Resident> residents = town.getResidents();
            if (player.getName().equals(town.getMayor().getName())) {
                return "MAYOR";
            } else {
               for (Resident resident : residents) {
                   if (resident.getName().equals(player.getName())) {
                       return "RESIDENT";
                   }
               }
            }
            return "NOTHING";
        } else {
            return null;
        }
    }
    public Town getTownForChunk(Chunk chunk) {
        WorldCoord worldcoord = new WorldCoord(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        try {
            return worldcoord.getTownBlock().getTown();
        } catch (NotRegisteredException ex) {
                return null;
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
            iConfig.integrationConfig.set("notice", "Towny integration does not utilize a config file.");
        }
        iConfig.saveIntegrationConfig();
    }

    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getTownForChunk(chunk).getName() + ChatColor.GREEN + " territory.");
        } else {
            return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is " + getRoleForChunk(chunk, player) + " to " + getTownForChunk(chunk).getName() + ".";
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk)) {
            if (getRoleForChunk(chunk, player) != null) {
                return "regenerator.regen.towny." + getRoleForChunk(chunk,player);
            } else {
                return "regenerator.regen.towny.OVERRIDE";
            }
        }
        return null;
    }
    
    @Override
    public boolean supportsUnknownProtectionDetection() {
        return false;
    }
}
