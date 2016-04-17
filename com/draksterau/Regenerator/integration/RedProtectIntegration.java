/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;

import br.net.fabiozumbi12.RedProtect.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import com.draksterau.Regenerator.config.integrationConfigHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public class RedProtectIntegration extends Integration {

    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        List<Region> regions = getRegionsForChunk(chunk);
        
        // If any of the regions in the chunk is wilderness, fail.
        for (Region region : regions) {
            if (getRegionsFromConfig().contains(region.getName())) {
                return false;
            }
        }
        
        if (!regions.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk)) {
            if (player.hasPermission(getPermissionRequiredToRegen(player, chunk))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean shouldChunkAutoRegen(Chunk chunk) {
        if (!isChunkClaimed(chunk)) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getRegionsFromConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        return iConfig.integrationConfig.getStringList("claimsAutoRegen");
    }
    @Override
    public void validateConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        iConfig.saveDefaultIntegrationConfig();
        List<String> claimsAutoRegen = Arrays.asList("adminRegion");
        if (!iConfig.integrationConfig.isSet("claimsAutoRegen")) {
            iConfig.integrationConfig.set("claimsAutoRegen", claimsAutoRegen);
        }
        for (String claimName : iConfig.integrationConfig.getStringList("claimsAutoRegen")) {
            if (!claimExists(claimName)) {
                RegeneratorPlugin.throwMessage("warning", "[" + this.getClass().getSimpleName() + "] Claim: " + claimName + " does not exist!");
                RegeneratorPlugin.disableIntegrationFor(RegeneratorPlugin.convertToModule(plugin));
            } else {
                RegeneratorPlugin.throwMessage("info", "[" + this.getClass().getSimpleName() + "] Claim: " + claimName + " detected. Whitelisting for automatic regeneration!");
            }
        }
        iConfig.saveIntegrationConfig();
    }


    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getRegionForChunk(chunk).getName() + ChatColor.GREEN + " region.");
        } else {
            if (isLeader(chunk, player)) {
                return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is LEADER to " +getRegionForChunk(chunk).getName() + ".";
            } else {
                if (isMember(chunk, player)) {
                    return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is MEMBER to " +getRegionForChunk(chunk).getName() + ".";
                } else {
                    return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is NOTHING to " +getRegionForChunk(chunk).getName() + ".";
                }
            }
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (isMember(chunk, player)) {
            return "regenerator.regen.redprotect.MEMBER";
        } else {
            if (isLeader(chunk, player)) {
                return "regenerator.regen.redprotect.LEADER";
            }
        }
        return null;
    }
    
    public boolean isMember(Chunk chunk, Player player) {
        List<Region> regions = getRegionsForChunk(chunk);
        for (Region region : regions) {
            if (region.isMember(player)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isLeader(Chunk chunk, Player player) {
        List<Region> regions = getRegionsForChunk(chunk);
        for (Region region : regions) {
            if (region.isLeader(player)) {
                return true;
            }
        }
        return false;
    }
    
    public int countRegionsForChunk(Chunk chunk) {
        return getRegionsForChunk(chunk).size();
    }
    
    public List<Region> getRegionsForChunk(Chunk chunk) {
        List<Region> regions = new ArrayList<Region>();
        for (Region region : RedProtect.rm.getRegionsByWorld(chunk.getWorld())) {
            int minChunkX = (int)Math.floor(region.getMinMbrX() / 16f);
            int maxChunkX = (int)Math.floor(region.getMaxMbrX() / 16f);
            int minChunkZ = (int)Math.floor(region.getMinMbrZ() / 16f);
            int maxChunkZ = (int)Math.floor(region.getMaxMbrZ() / 16f);
            if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {
                regions.add(region);
            }
        }
        return regions;
    }
    
    public Region getRegionForChunk(Chunk chunk) {
        if (countRegionsForChunk(chunk) == 1) {
            return getRegionsForChunk(chunk).get(0);
        }
        return null;
    }
    public boolean claimExists(String regionName) {
        for (World world : Bukkit.getWorlds()) {
            if (RedProtectAPI.getRegion(regionName, world) != null) {
                return true;
            }
        }
        return false;
    }
}
