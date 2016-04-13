/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.P;
import java.util.Arrays;
import java.util.List;
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

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldChunkAutoRegen(Chunk chunk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                RegeneratorPlugin.disableIntegrationFor(this.getPluginName());
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean factionUUIDExists(String factionName) {
        if (P.p.getFactionTags().contains(factionName)) {
            return true;
        } else {
            return false;
        }
    }
    
    
}
