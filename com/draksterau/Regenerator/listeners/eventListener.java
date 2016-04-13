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
            //throwMessage("info", "Unloading Chunk: {0} on world: {1}", new Object[]{event.getChunk(), event.getChunk().getWorld().getName()});
            if (RegeneratorPlugin.getConfig().getBoolean("regen-on-chunk-unload")) {
                if (RegeneratorPlugin.isLagOK()) {
                    if (RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = false;
                        RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator has resumed regeneration capabilities as TPS has risen to: " + (int)lagTask.getTps());
                    }
                    if (RegeneratorPlugin.validateChunkInactivity(event.getChunk(), false) && RegeneratorPlugin.autoRegenRequirementsMet(event.getChunk())) {
                            Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, event.getChunk()));
                            cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
                            cConfig.updateLastRegen();
                            RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
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
    public void onWorldLoad(WorldLoadEvent event) {
        if (RegeneratorPlugin.getConfig().getBoolean("regen-on-world-load")) {
            RegeneratorPlugin.tellAllNotified(ChatColor.RED + "Regenerator is regenerating all inactive chunks on: " + event.getWorld().getName() + ". Expect lag for a minute!");
            for (Chunk chunk : event.getWorld().getLoadedChunks()) {
                if (RegeneratorPlugin.validateChunkInactivity(chunk, true) && RegeneratorPlugin.autoRegenRequirementsMet(chunk)) {
                        Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, chunk));
                        chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, chunk);
                        cConfig = new chunkConfigHandler(RegeneratorPlugin, chunk);
                        cConfig.updateLastRegen();
                        RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + chunk.getX() + "," + chunk.getZ());
                } else {
                       //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                }
            }
            RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator is completed regenerating all inactive chunks on: " + event.getWorld().getName() + ". Expect lag for a minute!");
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldUnloadEvent event) {
        if (RegeneratorPlugin.getConfig().getBoolean("regen-on-world-unload")) {
            RegeneratorPlugin.tellAllNotified(ChatColor.RED + "Regenerator is regenerating all inactive chunks on: " + event.getWorld().getName() + ". Expect lag for a minute!");
            for (Chunk chunk : event.getWorld().getLoadedChunks()) {
                if (RegeneratorPlugin.validateChunkInactivity(chunk, false) && RegeneratorPlugin.autoRegenRequirementsMet(chunk)) {
                        Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, chunk));
                        chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, chunk);
                        cConfig = new chunkConfigHandler(RegeneratorPlugin, chunk);
                        cConfig.updateLastRegen();
                        RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + chunk.getX() + "," + chunk.getZ());
                } else {
                       //RegeneratorPlugin.throwMessage("info", "Ignoring Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
                }
            }
            RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator is completed regenerating all inactive chunks on: " + event.getWorld().getName() + ". Expect lag for a minute!");
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
                    if (RegeneratorPlugin.validateChunkInactivity(fromChunk, true) && RegeneratorPlugin.autoRegenRequirementsMet(fromChunk)) {
                            Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, fromChunk));
                            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, fromChunk);
                            cConfig = new chunkConfigHandler(RegeneratorPlugin, fromChunk);
                            cConfig.updateLastRegen();
                            RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + fromChunk.getX() + "," + fromChunk.getZ());
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
    public void onChunkLoad(ChunkLoadEvent event) {
        worldConfigHandler wConfig = new worldConfigHandler(RegeneratorPlugin, event.getChunk().getWorld());
        if (wConfig.getAutoRegen() || wConfig.getManualRegen()) {
            chunkConfigHandler cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
            cConfig.updateLastLoaded();
            //throwMessage("info", "Unloading Chunk: {0} on world: {1}", new Object[]{event.getChunk(), event.getChunk().getWorld().getName()});
            if (RegeneratorPlugin.getConfig().getBoolean("regen-on-chunk-load")) {
                if (RegeneratorPlugin.isLagOK()) {
                    if (RegeneratorPlugin.isPaused) {
                        RegeneratorPlugin.isPaused = false;
                        RegeneratorPlugin.tellAllNotified(ChatColor.GREEN + "Regenerator has resumed regeneration capabilities as TPS has risen to: " + (int)lagTask.getTps());
                    }
                    if (RegeneratorPlugin.validateChunkInactivity(event.getChunk(), true) && RegeneratorPlugin.autoRegenRequirementsMet(event.getChunk())) {
                            Bukkit.getServer().getScheduler().runTask(RegeneratorPlugin, new ChunkTask(RegeneratorPlugin, event.getChunk()));
                            cConfig = new chunkConfigHandler(RegeneratorPlugin, event.getChunk());
                            cConfig.updateLastRegen();
                            RegeneratorPlugin.throwMessage("info", "Regenerating Chunk at: " + event.getChunk().getX() + "," + event.getChunk().getZ());
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
