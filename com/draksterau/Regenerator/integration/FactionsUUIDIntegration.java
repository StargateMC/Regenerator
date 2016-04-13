/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public final class FactionsUUIDIntegration extends Integration {

    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX(), 100.0, chunk.getZ());
        FLocation floc = new FLocation(loc);
        if (!getFactionsUUIDFromConfig().contains(Board.getInstance().getFactionAt(floc).getTag())) {
            return true;
        } else {
            return false;
        }
    }

    public Faction getFactionForChunk(Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX(), 100.0, chunk.getZ());
        FLocation floc = new FLocation(loc);
        if (!getFactionsUUIDFromConfig().contains(Board.getInstance().getFactionAt(floc).getTag())) {
            return Board.getInstance().getFactionAt(floc);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        FPlayer fp = FPlayers.getInstance().getByPlayer(player);
        Faction f = fp.getFaction();
        if (isChunkClaimed(chunk)) {
            if (f.equals(getFactionForChunk(chunk)) && player.hasPermission("regenerator.regen.factionsuuid.FACTION")) {
                return true;
            } else {
                if (player.hasPermission("regenerator.regen.factionsuuid.OTHER") && !getFactionsUUIDFromConfig().contains(getFactionForChunk(chunk))) {
                    return true;
                } else {
                    return false;
                }
            }
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
        List<String> factionsUUIDAutoRegen = Arrays.asList("WILDERNESS");
        if (!iConfig.integrationConfig.isSet("factionsUUIDAutoRegen")) {
            iConfig.integrationConfig.set("factionsUUIDAutoRegen", factionsUUIDAutoRegen);
        }
        for (String factionName : iConfig.integrationConfig.getStringList("factionsUUIDAutoRegen")) {
            if (!factionUUIDExists(factionName)) {
                RegeneratorPlugin.throwMessage("severe", "Faction: " + factionName + " does not exist!");
                RegeneratorPlugin.disableIntegrationFor(RegeneratorPlugin.convertToModule(plugin));
            }
        }
        iConfig.saveIntegrationConfig();    
    }

    public List<String> getFactionsUUIDFromConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        return iConfig.integrationConfig.getStringList("factionsUUIDAutoRegen");
    }
    
    
    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        FPlayer fp = FPlayers.getInstance().getByPlayer(player);
        Faction f = fp.getFaction();
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getFactionForChunk(chunk).getTag() + ChatColor.GREEN + " territory.");
        } else {
            if (!getFactionsUUIDFromConfig().contains(getFactionForChunk(chunk))) {
                return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually due to your relation with " + getFactionForChunk(chunk).getTag() + ".";
            } else {
                return (ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as it is unclaimed.");
            }
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        FPlayer fp = FPlayers.getInstance().getByPlayer(player);
        Faction f = fp.getFaction();
        if (f.equals(getFactionForChunk(chunk))) {
            return "regenerator.regen.factionsuuid.FACTION";
        } else {
            if (!getFactionsUUIDFromConfig().contains(getFactionForChunk(chunk))) {
                return "regenerator.regen.factionsuuid.OTHER";
            } else {
                return "regenerator.regen.factionsuuid.UNCLAIMED";
            }
        }
    }

    private boolean factionUUIDExists(String factionName) {
        if (Factions.getInstance().isTagTaken(factionName)) {
            return true;
        } else {
            return false;
        }
    }
    
    
}
