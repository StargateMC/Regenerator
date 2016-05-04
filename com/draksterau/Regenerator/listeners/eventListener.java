/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.listeners;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.tasks.regenTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
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
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk());
        RegeneratorPlugin.loadedChunks.add(RChunk);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk());
        RegeneratorPlugin.loadedChunks.remove(RChunk);
    }

    /// END CHUNK EVENTS ///
    
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
