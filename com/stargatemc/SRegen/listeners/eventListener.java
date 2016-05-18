/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stargatemc.SRegen.listeners;

import com.stargatemc.SRegen.SRegen;
import com.stargatemc.SRegen.handlers.RChunk;
import com.stargatemc.SRegen.handlers.RWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
    
    SRegen SRegenPlugin;
    
    public eventListener (SRegen SRegenPlugin) {
        this.SRegenPlugin = SRegenPlugin;
    }
    
    
    /// START WORLD EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        // Load the RWorld from the filesystem.
        RWorld RWorld = new RWorld(SRegenPlugin, event.getWorld());
        // If the Plugin currently hasnt got the RWorld registered, register it.
        if (!SRegenPlugin.loadedWorlds.contains(RWorld)) SRegenPlugin.loadedWorlds.add(RWorld);
        SRegenPlugin.utils.throwMessage("info", "Loaded World : " + event.getWorld().getName());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        RWorld RWorld = new RWorld(SRegenPlugin, event.getWorld());
        // If the Plugin currently has the RWorld registered, removed it.
        if (SRegenPlugin.loadedWorlds.contains(RWorld)) SRegenPlugin.loadedWorlds.remove(RWorld);
        SRegenPlugin.utils.throwMessage("info", "Unloaded World : " + event.getWorld().getName());
    }
    
    /// END WORLD EVENTS ///
    
    /// START CHUNK EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        RChunk RChunk = new RChunk(SRegenPlugin, event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
        // Do nothing. This constructor for the RChunk will generate its entry in the data file if needed.
    }

    /// END CHUNK EVENTS ///
    
    // START BLOCK EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        RChunk rChunk = new RChunk(SRegenPlugin, event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock().getWorld().getName());
        if (SRegenPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk())) rChunk.updateActivity();
        if (!SRegenPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk()) && rChunk.lastActivity != 0) rChunk.resetActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        RChunk rChunk = new RChunk(SRegenPlugin, event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock().getWorld().getName());
        if (SRegenPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk())) rChunk.updateActivity();
        if (!SRegenPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk()) && rChunk.lastActivity != 0) rChunk.resetActivity();
    }
    
    // END BLOCK EVENTS ///
    
    // START PLUGIN EVENTS ///
    
    public void onPluginEnable(PluginEnableEvent event) {
        if (SRegenPlugin.utils.convertToModule(event.getPlugin().getName()) != null) {
            SRegenPlugin.utils.loadIntegrationFor(SRegenPlugin.utils.convertToModule(event.getPlugin().getName()));
        }        
    }
    
    public void onPluginDisable(PluginDisableEvent event) {
        if (SRegenPlugin.utils.convertToModule(event.getPlugin().getName()) != null) {
            SRegenPlugin.utils.disableIntegrationFor(SRegenPlugin.utils.convertToModule(event.getPlugin().getName()));
        }        
    }
    
    // END PLUGIN EVENTS ///
}
