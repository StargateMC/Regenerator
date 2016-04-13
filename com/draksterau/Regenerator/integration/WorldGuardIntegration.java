/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;
import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
 
/**
 *
 * @author draks
 */
public class WorldGuardIntegration extends Integration {

    // Regenerator code for WG is based off: https://bukkit.org/threads/check-if-player-is-in-worldguard-region.29241/

    @Override
    public boolean isChunkClaimed(Chunk chunk) {
        int bx = chunk.getX() << 4;
        int bz = chunk.getZ() << 4;
        BlockVector pt1 = new BlockVector(bx, 0, bz);
        BlockVector pt2 = new BlockVector(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);        
        ApplicableRegionSet regions = WGBukkit.getRegionManager(chunk.getWorld()).getApplicableRegions(region);
        
        // This checks to see if any of the regions in a chunk are NOT wilderness.
        for (ProtectedRegion listedRegion : regions) {
            if (!getRegionsFromConfig().contains(listedRegion.getId())) {
                // Region is not claimed, it is wilderness!
                return true;
            }
        }
        return false;
    }

    public ProtectedRegion getRegionForChunk(Chunk chunk) {
        int bx = chunk.getX() << 4;
        int bz = chunk.getZ() << 4;
        BlockVector pt1 = new BlockVector(bx, 0, bz);
        BlockVector pt2 = new BlockVector(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);        
        ApplicableRegionSet regions = WGBukkit.getRegionManager(chunk.getWorld()).getApplicableRegions(region);
        
        // This checks to see if any of the regions in a chunk are NOT wilderness.
        for (ProtectedRegion listedRegion : regions) {
            if (!getRegionsFromConfig().contains(listedRegion.getId())) {
                // Region is claimed, it is not wilderness!
                return listedRegion;
            }
        }
        return null;
    }
    
    public int getCountRegions(Chunk chunk) {
        int count = 0;
        int bx = chunk.getX() << 4;
        int bz = chunk.getZ() << 4;
        BlockVector pt1 = new BlockVector(bx, 0, bz);
        BlockVector pt2 = new BlockVector(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);        
        ApplicableRegionSet regions = WGBukkit.getRegionManager(chunk.getWorld()).getApplicableRegions(region);
        
        // This checks to see if any of the regions in a chunk are NOT wilderness.
        for (ProtectedRegion listedRegion : regions) {
            if (!getRegionsFromConfig().contains(listedRegion.getId())) {
                // Region is claimed, it is not wilderness!
                count++;
            }
        }
        return count;
    }
    
    @Override
    public boolean canPlayerRegen(Player player, Chunk chunk) {
        if (isChunkClaimed(chunk)) {
            if (player.hasPermission(getPermissionRequiredToRegen(player, chunk)) && getPermissionRequiredToRegen(player,chunk) != null) {
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

    @Override
    public void validateConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        iConfig.saveDefaultIntegrationConfig();
        List<String> regionsAutoRegen = Arrays.asList("__global__");
        if (!iConfig.integrationConfig.isSet("regionsAutoRegen")) {
            iConfig.integrationConfig.set("regionsAutoRegen", regionsAutoRegen);
        }
        for (String regionName : iConfig.integrationConfig.getStringList("regionsAutoRegen")) {
            if (!regionExists(regionName)) {
                RegeneratorPlugin.throwMessage("severe", "Region: " + regionName + " does not exist!");
                RegeneratorPlugin.disableIntegrationFor(this.getPluginName());
            }
        }
        iConfig.saveIntegrationConfig();    
    }

    @Override
    public String getPlayerRegenReason(Player player, Chunk chunk) {
        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in the " + ChatColor.BLUE + getRegionForChunk(chunk).getId() + ChatColor.GREEN + " region.");
        } else {
            if (isOwner(getRegionForChunk(chunk), player)) {
                return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is OWNER to " + getRegionForChunk(chunk).getId() + ".";
            } else {
                if (isMember(getRegionForChunk(chunk), player)) {
                    return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is MEMBER to " + getRegionForChunk(chunk).getId() + ".";
                } else {
                    return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is NOTHING to " + getRegionForChunk(chunk).getId() + ".";
                }
            }
        }
    }

    @Override
    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
        if (isOwner(getRegionForChunk(chunk),player)) {
            return "regenerator.regen.worldguard.OWNER";
        } else {
            if (isMember(getRegionForChunk(chunk), player)) {
                return "regenerator.regen.worldguard.MEMBER";
            }
        }
        return null;
    }
    
    public boolean regionExists(String regionSearchingFor) {
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = WGBukkit.getRegionManager(world);
            Map< String, ProtectedRegion > worldRegions = regionManager.getRegions(); 
            for ( ProtectedRegion region : worldRegions.values()) {
                if (region.getId().equals(regionSearchingFor)) {
                    return true;
                }
            }
        }
        return false;
    }
    public List<String> getRegionsFromConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        return iConfig.integrationConfig.getStringList("regionsAutoRegen");
    }
    
    public boolean isOwner(ProtectedRegion region, Player player) {
        if (region.getOwners().contains(player.getUniqueId())) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isMember(ProtectedRegion region, Player player) {
        if (region.getMembers().contains(player.getUniqueId())) {
            return true;
        } else {
            return false;
        }
    }
}