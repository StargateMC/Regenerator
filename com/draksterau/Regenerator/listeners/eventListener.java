/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.listeners;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RUtils;
import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.event.RegenerationRequestEvent;
import com.draksterau.Regenerator.event.RequestTrigger;
import com.draksterau.Regenerator.tasks.ChunkTask;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.Location;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 *
 * @author draks
 */
public class eventListener implements Listener {
    
    RegeneratorPlugin RegeneratorPlugin;
    
    public eventListener (RegeneratorPlugin RegeneratorPlugin) {
        this.RegeneratorPlugin = RegeneratorPlugin;
    }
    
    
    /// START WORLD EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        // Load the RWorld from the filesystem.
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently hasnt got the RWorld registered, register it.
        if (!RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.add(RWorld);
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getWorld().getSpawnLocation().getBlockX(), event.getWorld().getSpawnLocation().getBlockX(), event.getWorld().getName());
        RegeneratorPlugin.utils.throwMessage("info", "Loaded World : " + event.getWorld().getName());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently has the RWorld registered, removed it.
        if (RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.remove(RWorld);
        RegeneratorPlugin.utils.throwMessage("info", "Unloaded World : " + event.getWorld().getName());
    }
    
    /// END WORLD EVENTS ///
    
    /// START CHUNK EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (RegeneratorPlugin.config.cacheChunksOnLoad) {
            RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
            if (RegeneratorPlugin.config.enableRegenerationNextChunkLoad && RChunk.lastActivity == -1) {
                Location loc = new Location(event.getWorld(), event.getChunk().getX() * 16, 100, event.getChunk().getZ() * 16);
                RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(loc, null, RequestTrigger.ChunkLoad, RegeneratorPlugin);
                Bukkit.getServer().getPluginManager().callEvent(requestEvent);
            }
        }
        // Do nothing. This constructor for the RChunk will generate its entry in the data file if needed.
    }

    /// END CHUNK EVENTS ///
    
    // START BLOCK EVENTS ///
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().equals(RegeneratorPlugin.fakePlayer)) return;
        RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(event.getBlock().getLocation(), event.getPlayer(), RequestTrigger.Break, this.RegeneratorPlugin);
        Bukkit.getServer().getPluginManager().callEvent(requestEvent);     
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBreakCheck(BlockBreakEvent event) {
        if (!event.getPlayer().equals(RegeneratorPlugin.fakePlayer)) return;
        if (RUtils.breakAndResult.containsKey(event.getBlock().getLocation())) {
            RegeneratorPlugin.utils.throwMessage("info", "Found result for break check: " + event.isCancelled() + " at : " + event.getBlock().getLocation().toString());
            RUtils.breakAndResult.replace(event.getBlock().getLocation(), !event.isCancelled());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onRegenerationRequest(RegenerationRequestEvent event) {
        RChunk rChunk = new RChunk(RegeneratorPlugin, event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock().getWorld().getName());
        if (!event.isCancelled() && !event.getTrigger().equals(RequestTrigger.Command)) {
            if (event.isImmediate()) {
                try {
                    new ChunkTask(rChunk, false).runTask(RegeneratorPlugin);
                } catch (Exception e) {
                    RegeneratorPlugin.utils.throwMessage("severe", "Failed to regenerate chunk : " + rChunk.getChunk().getX() + "," + rChunk.getChunk().getZ() + " on world: " + rChunk.getWorldName());
                    e.printStackTrace();
                }
            } else {
                if (RegeneratorPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk())) rChunk.updateActivity();
            }
        }
        if (!RegeneratorPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk()) && rChunk.lastActivity != 0) rChunk.resetActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(event.getBlock().getLocation(), event.getPlayer(), RequestTrigger.Place, this.RegeneratorPlugin);
        Bukkit.getServer().getPluginManager().callEvent(requestEvent);       
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent  event) {
        ArrayList<Chunk> chunksRequested = new ArrayList<Chunk>();
        for (Block b : event.blockList()) {
            if (chunksRequested.contains(b.getChunk())) continue;
            RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(b.getLocation(), null, RequestTrigger.Explosion, this.RegeneratorPlugin);
            Bukkit.getServer().getPluginManager().callEvent(requestEvent);
            if (!requestEvent.isCancelled()) chunksRequested.add(b.getChunk());
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent  event) {
        ArrayList<Chunk> chunksRequested = new ArrayList<Chunk>();
        for (Block b : event.blockList()) {
            if (chunksRequested.contains(b.getChunk())) continue;
            RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(b.getLocation(), null, RequestTrigger.Explosion, this.RegeneratorPlugin);
            Bukkit.getServer().getPluginManager().callEvent(requestEvent);
            if (!requestEvent.isCancelled()) chunksRequested.add(b.getChunk());
        }
    }
    // END BLOCK EVENTS ///
    
    // START PLUGIN EVENTS ///
    
    public void onPluginEnable(PluginEnableEvent event) {
        if (RegeneratorPlugin.utils.convertToModule(event.getPlugin().getName()) != null) {
            RegeneratorPlugin.utils.loadIntegrationFor(RegeneratorPlugin.utils.convertToModule(event.getPlugin().getName()));
        }        
    }
    
    public void onPluginDisable(PluginDisableEvent event) {
        if (RegeneratorPlugin.utils.convertToModule(event.getPlugin().getName()) != null) {
            RegeneratorPlugin.utils.disableIntegrationFor(RegeneratorPlugin.utils.convertToModule(event.getPlugin().getName()));
        }        
    }
    
    // END PLUGIN EVENTS ///
}
