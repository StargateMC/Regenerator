/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.integration;
import com.draksterau.Regenerator.Handlers.MsgType;
import com.draksterau.Regenerator.config.integrationConfigHandler;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Set;
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
        BlockVector3 pt1 =  BlockVector3.at(bx, 0, bz);
        BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);    
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(chunk.getWorld())).getApplicableRegions(region);        
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
        BlockVector3 pt1 =  BlockVector3.at(bx, 0, bz);
        BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);        
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(chunk.getWorld())).getApplicableRegions(region);
        
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
        BlockVector3 pt1 =  BlockVector3.at(bx, 0, bz);
        BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("RegeneratorTEMP", pt1, pt2);     
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(chunk.getWorld())).getApplicableRegions(region);
        
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
            if (player.hasPermission(getPermissionRequiredToRegen(player, chunk))) {
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
        List<String> claimsAutoRegen = Arrays.asList("__global__");
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
            } else {
                return "regenerator.regen.worldguard.OVERRIDE";
            }
        }
    }
    
    public boolean claimExists(String regionSearchingFor) {
        for (World world : Bukkit.getWorlds()) {
            Set<String> regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world)).getRegions().keySet();
            if (regions.contains(regionSearchingFor)) return true;
        }
        return false;
    }
    public List<String> getRegionsFromConfig() {
        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
        return iConfig.integrationConfig.getStringList("claimsAutoRegen");
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
    @Override
    public boolean supportsUnknownProtectionDetection() {
        return true;
    }
}
