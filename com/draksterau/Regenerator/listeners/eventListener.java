/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.listeners;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.draksterau.Regenerator.config.chunkConfigHandler;
import com.draksterau.Regenerator.config.worldConfigHandler;
import com.draksterau.Regenerator.tasks.ChunkTask;
import com.draksterau.Regenerator.tasks.lagTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author draks
 */
public class eventListener implements Listener {
    
    RegeneratorPlugin RegeneratorPlugin;
    
    public eventListener (RegeneratorPlugin RegeneratorPlugin) {
        this.RegeneratorPlugin = RegeneratorPlugin;
    }
    
    // THIS IS THE EVENTS THAT UPDATE A CHUNK.
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getChunk().getWorld());
        if (wConfig.getAutoRegen() || wConfig.getManualRegen()) {
            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
            cConfig.updateLastUnloaded();
            if (RegeneratorPlugin.getIntegrationForChunk(event.getChunk()) != null && (cConfig.getLastUnclaimed() > cConfig.getLastClaimed() || cConfig.getLastClaimed() == 0)) {
                cConfig.updateLastClaimed();
            }
            if (RegeneratorPlugin.getIntegrationForChunk(event.getChunk()) == null && cConfig.getLastUnclaimed() < cConfig.getLastClaimed()) {
                cConfig.updateLastUnclaimed();
            }
            //throwMessage("info", "Unloading Chunk: {0} on world: {1}", new Object[]{event.getChunk(), event.getChunk().getWorld().getName()});
            if (RegeneratorPlugin.getConfig().getBoolean("regen-on-chunk-unload")) {
                if (RegeneratorPlugin.isLagOK()) {
                    if (RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = false;
                        RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator has resumed regeneration capabilities as TPS has risen to: " + (int)lagTask.getTps());
                    }
                    if (RegeneratorPlugin.validateChunkInactivity(event.getChunk(), false)) {
                        if (RegeneratorPlugin.autoRegenRequirementsMet(event.getChunk())) {
                            Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, event.getChunk()));
                            cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
                            cConfig.updateLastRegen();
                            RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ() + " on world: " + event.getWorld().getName());
                        } else {
                           //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                        }
                    } else {
                           //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                    }
                } else {
                    if (!RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = true;
                        RegeneratorPlugin.tellAllNotified(ChatColor.RED + "Regenerator has suspended regeneration capabilities as TPS has dropped to: " + (int)lagTask.getTps());
                    }            
                }
            }
        } 

    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk toChunk = event.getTo().getChunk();
        Chunk fromChunk = event.getFrom().getChunk();
        if (RegeneratorPlugin.getConfig().getBoolean("regen-on-player-change-chunk")) {
            if (fromChunk != toChunk) {
                if (RegeneratorPlugin.isLagOK()) {
                    if (RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = false;
                        RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator has resumed regeneration capabilities as TPS has risen to: " + (int)lagTask.getTps());
                    }
                    long z;
                    long x;
                    for (z = ((fromChunk.getZ()*16)-(RegeneratorPlugin.getConfig().getLong("regen-on-player-change-chunk-range"))); z <= ((fromChunk.getZ()*16) + (RegeneratorPlugin.getConfig().getLong("regen-on-player-change-chunk-range"))); z = z + 16) {
                        for (x = ((fromChunk.getX()*16)-(RegeneratorPlugin.getConfig().getLong("regen-on-player-change-chunk-range"))); x <= ((fromChunk.getX()*16) + (RegeneratorPlugin.getConfig().getLong("regen-on-player-change-chunk-range"))); x = x + 16) {
                            Location loc = new Location(fromChunk.getWorld(), x, 100.0, z);
                            Chunk toRegenerate = fromChunk.getWorld().getChunkAt(loc);
                            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, toRegenerate);
                            if (RegeneratorPlugin.getIntegrationForChunk(toRegenerate) != null && (cConfig.getLastUnclaimed() > cConfig.getLastClaimed() || cConfig.getLastClaimed() == 0)) {
                                cConfig.updateLastClaimed();
                            }
                            if (RegeneratorPlugin.getIntegrationForChunk(toRegenerate) == null && cConfig.getLastUnclaimed() < cConfig.getLastClaimed()) {
                                cConfig.updateLastUnclaimed();
                            }
                            if (RegeneratorPlugin.validateChunkInactivity(toRegenerate, true)) {
                                if (RegeneratorPlugin.autoRegenRequirementsMet(toRegenerate)) {
                                    Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, toRegenerate));
                                    cConfig.updateLastRegen();
                                    RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + toRegenerate.getX() + "," + toRegenerate.getZ() + " on world: " + toRegenerate.getWorld().getName());
                                } else {
                                  // RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + toRegenerate.getX() + "," + toRegenerate.getZ());
                                }
                            } else {
                                  // RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + toRegenerate.getX() + "," + toRegenerate.getZ());
                            }
                        }
                    }
                } else {
                    if (!RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = true;
                        RegeneratorPlugin.tellAllNotified(ChatColor.RED + "Regenerator has suspended regeneration capabilities as TPS has dropped to: " + (int)lagTask.getTps());
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerChangedWorldEvent event) {
        if (event.getPlayer().hasPermission("regenerator.worldchange.notify")) {
            worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getPlayer().getWorld());
            if (wConfig.getAutoRegen() == true) {
                event.getPlayer().sendMessage(RegeneratorPlugin.getFancyName() + ChatColor.RED + "Warning:" + ChatColor.GRAY + " Unclaimed land on this world will regenerate after it is inactive for : " + ChatColor.AQUA + (wConfig.getChunkInterval() / 60) + ChatColor.GRAY + " minutes");
            } else {
                event.getPlayer().sendMessage(RegeneratorPlugin.getFancyName() + ChatColor.BLUE + "Note:" + ChatColor.GRAY + " Unclaimed land on this world will not automatically regenerate.");
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getChunk().getWorld());
        if (wConfig.getAutoRegen() || wConfig.getManualRegen()) {
            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
            cConfig.updateLastLoaded();
            if (RegeneratorPlugin.getIntegrationForChunk(event.getChunk()) != null && (cConfig.getLastUnclaimed() > cConfig.getLastClaimed() || cConfig.getLastClaimed() == 0)) {
                cConfig.updateLastClaimed();
            }
            if (RegeneratorPlugin.getIntegrationForChunk(event.getChunk()) == null && cConfig.getLastUnclaimed() < cConfig.getLastClaimed()) {
                cConfig.updateLastUnclaimed();
            }
            //throwMessage("info", "Unloading Chunk: {0} on world: {1}", new Object[]{event.getChunk(), event.getChunk().getWorld().getName()});
            if (RegeneratorPlugin.getConfig().getBoolean("regen-on-chunk-load")) {
                if (RegeneratorPlugin.isLagOK()) {
                    if (RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = false;
                        RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator has resumed regeneration capabilities as TPS has risen to: " + (int)lagTask.getTps());
                    }
                    if (RegeneratorPlugin.validateChunkInactivity(event.getChunk(), true)) {
                        if (RegeneratorPlugin.autoRegenRequirementsMet(event.getChunk())) {
                            Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, event.getChunk()));
                            cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
                            cConfig.updateLastRegen();
                            RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ() + " on world: " + event.getWorld().getName());
                        } else {
                           //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                        }
                    } else {
                           //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                    }
                } else {
                    if (!RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = true;
                        RegeneratorPlugin.tellAllNotified(ChatColor.RED + "Regenerator has suspended regeneration capabilities as TPS has dropped to: " + (int)lagTask.getTps());
                    }
                }
            }
        } 
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getBlock().getChunk().getWorld());
        if (wConfig.getAutoRegen() || wConfig.getManualRegen()) {
            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getBlock().getChunk());
            cConfig.updateLastBroken();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getBlock().getChunk().getWorld());
        if (wConfig.getAutoRegen() || wConfig.getManualRegen()) {
            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getBlock().getChunk());
            cConfig.updateLastPlaced();
        }
    }
    
    

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin (PlayerJoinEvent event) {
        if (RegeneratorPlugin.getConfig().getBoolean("general.welcome")) {
            event.getPlayer().sendMessage(RegeneratorPlugin.getFancyName() + "This server is running Regenerator v" + ChatColor.AQUA + RegeneratorPlugin.getDescription().getVersion() + ChatColor.GRAY + " by Bysokar for automatic terrain regeneration.");
        }
    }
}
