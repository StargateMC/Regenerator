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
import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RWorld;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        // Load the RWorld from the filesystem.
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently hasnt got the RWorld registered, register it.
        if (!RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.add(RWorld);
        RegeneratorPlugin.throwMessage("info", "Loaded World : " + event.getWorld().getName());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently has the RWorld registered, removed it.
        if (RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.remove(RWorld);
        RegeneratorPlugin.throwMessage("info", "Unloaded World : " + event.getWorld().getName());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk());
        RegeneratorPlugin.loadedChunks.add(RChunk);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk());
        RegeneratorPlugin.loadedChunks.remove(RChunk);
    }

}
