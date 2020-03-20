/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.Handlers.MsgType;
import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.Rank;
import com.massivecraft.massivecore.ps.PS;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;


/**
 *
 * @author draks
 */
public final class FactionsIntegration extends Integration {

    // INTEGRATION METHODS ARE HERE.

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
            return ("regenerator.regen.factions." + getPlayerRelation(player,chunk).name());
    }
    
    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getFactionForChunk(chunk).getName() + ChatColor.GREEN + " territory.");
        } else {
            return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is " + getPlayerRelation(player,chunk).name() + " to " + getFactionForChunk(chunk).getName() + ".";
        }
    }
    
    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        if (!getFactionsFromConfig().contains(getFactionForChunk(chunk).getName()) && !getFactionForChunk(chunk).getId().equals("none")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk)) {
            if (player.hasPermission("regenerator.regen.factions." + getPlayerRelation(player,chunk).name())) {
                return true;
            } else {
                return false;
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
    
    public List<String> getFactionsFromConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        return iConfig.integrationConfig.getStringList("claimsAutoRegen");
    }
    
    @Override
    public void validateConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        iConfig.saveDefaultIntegrationConfig();
        List<String> claimsAutoRegen = Arrays.asList("Wilderness");
        if (!iConfig.integrationConfig.isSet("claimsAutoRegen")) {
            iConfig.integrationConfig.set("claimsAutoRegen", claimsAutoRegen);
        }
        for (String claimName : iConfig.integrationConfig.getStringList("claimsAutoRegen")) {
            if (!claimExists(claimName)) {
                RegeneratorPlugin.utils.throwMessage(MsgType.WARNING, "[" + this.getClass().getSimpleName() + "] Claim: " + claimName + " does not exist!");
                RegeneratorPlugin.utils.disableIntegrationFor(RegeneratorPlugin.utils.convertToModule(plugin));
            } else {
                RegeneratorPlugin.utils.throwMessage(MsgType.INFO, "[" + this.getClass().getSimpleName() + "] Claim: " + claimName + " detected. Whitelisting for automatic regeneration!");
            }
        }
        iConfig.saveIntegrationConfig();
    }
    
    // FACTIONS SPECIFIC METHODS ARE HERE.
    
    public static Faction getFactionForChunk(Chunk chunk) {
        PS psChunk = PS.valueOf(chunk);
        Faction factionAtChunk = BoardColl.get().getFactionAt(psChunk);
        return factionAtChunk;
    }
    
    public static boolean claimExists(String name) {
        Faction faction = FactionColl.get().getByName(name);
        return faction instanceof Faction;
    }

    
    public String getRelationBetweenFactions(String faction1Name, String faction2Name) {
        Faction faction2 = FactionColl.get().getByName(faction2Name);
        Rel relation = FactionColl.get().getByName(faction1Name).getRelationTo(faction2);
        return relation.getName();
    }

    public Rank getFactionRole(MPlayer player) {
        return player.getRank();
    }

    
    public static Rel getPlayerRelation(Player player, Chunk chunk) {
        MPlayer factionPlayer = MPlayer.get(player);
        Faction chunkFaction = getFactionForChunk(chunk);
        return factionPlayer.getRelationTo(chunkFaction, true);
    }

    @Override
    public boolean supportsUnknownProtectionDetection() {
        return true;
    }

}
