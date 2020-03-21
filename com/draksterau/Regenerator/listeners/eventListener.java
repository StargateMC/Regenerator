/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.listeners;

import com.draksterau.Regenerator.Handlers.MsgType;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
        if (event.getWorld().getName().equals("worldeditregentempworld")) {
            //RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Skipping " + event.getEventName() + " for: worldeditregentempworld");
            return;
        }
        // Load the RWorld from the filesystem.
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently hasnt got the RWorld registered, register it.
        if (!RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.add(RWorld);
        RChunk RChunk = new RChunk(RegeneratorPlugin, event.getWorld().getSpawnLocation().getBlockX(), event.getWorld().getSpawnLocation().getBlockX(), event.getWorld().getName());
        RegeneratorPlugin.utils.throwMessage(MsgType.INFO, "Loaded World : " + event.getWorld().getName());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        if (event.getWorld().getName().equals("worldeditregentempworld")) {
            //RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Skipping " + event.getEventName() + " for: worldeditregentempworld");
            return;
        }
        RWorld RWorld = new RWorld(RegeneratorPlugin, event.getWorld());
        // If the Plugin currently has the RWorld registered, removed it.
        if (RegeneratorPlugin.loadedWorlds.contains(RWorld)) RegeneratorPlugin.loadedWorlds.remove(RWorld);
        RegeneratorPlugin.utils.throwMessage(MsgType.INFO, "Unloaded World : " + event.getWorld().getName());
    }
    
    /// END WORLD EVENTS ///
    
    // START PLAYER EVENTS //
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersRadius == -1) return;
        if (event.getTo().getChunk().equals(event.getFrom().getChunk())) return;
        for (RChunk chunk : RegeneratorPlugin.utils.getRChunksNear(event.getTo(), RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersRadius)) {
            Location loc = new Location(event.getTo().getWorld(), chunk.getChunk().getX() * 16, 100, chunk.getChunk().getZ() * 16);
            RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(loc, event.getPlayer(), RequestTrigger.PlayerMovement, RegeneratorPlugin);
            if (this.RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersInstant) requestEvent.setIsImmediate(true);
            Bukkit.getServer().getPluginManager().callEvent(requestEvent);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersRadius == -1) return;
        if (event.getTo().getChunk().equals(event.getFrom().getChunk())) return;
        for (RChunk chunk : RegeneratorPlugin.utils.getRChunksNear(event.getTo(), RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersRadius)) {
            Location loc = new Location(event.getTo().getWorld(), chunk.getChunk().getX() * 16, 100, chunk.getChunk().getZ() * 16);
            RegenerationRequestEvent requestEvent = new RegenerationRequestEvent(loc, event.getPlayer(), RequestTrigger.PlayerMovement, RegeneratorPlugin);
            if (this.RegeneratorPlugin.config.regenerateUninitialisedChunksNearPlayersInstant) requestEvent.setIsImmediate(true);
            Bukkit.getServer().getPluginManager().callEvent(requestEvent);
        }
    }
    
    // END PLAYER EVENTS //
    /// START CHUNK EVENTS ///
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().getName().equals("worldeditregentempworld")) {
            //RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Skipping " + event.getEventName() + " for: worldeditregentempworld");
            return;
        }
        if (RegeneratorPlugin.config.cacheChunksOnLoad) {
            if (!RegeneratorPlugin.utils.isLagOK()) {
                RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Skipping " + event.getEventName() + " as TPS is too low. This means the chunk will not be cached until the chunk is modified by a player or the chunk is reloaded.");
                return;
            }
            RChunk RChunk = new RChunk(RegeneratorPlugin, event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
            RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Initialised " + (RChunk.lastActivity == -1 ? " new " : " existing ") + " chunk: " + event.getChunk().getX() + "," + event.getChunk().getZ() + " on world: " + event.getChunk().getWorld().getName());
        } else {
            RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Skipping " + event.getEventName() + " as cacheChunksOnLoad is disabled");
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
            RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Found result for unknownProtectionDetection break check: " + event.isCancelled() + " at : " + event.getBlock().getLocation().toString());
            RUtils.breakAndResult.replace(event.getBlock().getLocation(), !event.isCancelled());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onRegenerationRequest(RegenerationRequestEvent event) {
        RChunk rChunk = new RChunk(RegeneratorPlugin, event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock().getWorld().getName());
        RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Handling regeneration request for : " + event.getChunk().getX() + "," + event.getChunk().getZ() + " on behalf of " + (event.getRequestor() != null ? event.getRequestor().getName() : " No-one" ) + " on world: " + event.getWorld().getName() + " with trigger: " + event.getTrigger().name() + " that " + (event.isImmediate() ? " is immediate " : " is not immediate ") + " and " + (event.isCancelled() ? "is cancelled" : " is not cancelled"));
        if (!event.isCancelled() && !event.getTrigger().equals(RequestTrigger.Command)) {
            if (event.isImmediate() && RegeneratorPlugin.utils.isLagOK()) {
                try {
                    RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Requesting regeneration of chunk: " + rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.worldName);
                    new ChunkTask(rChunk, false).runTask(RegeneratorPlugin);
                } catch (Exception e) {
                    RegeneratorPlugin.utils.throwMessage(MsgType.SEVERE, "Failed to regenerate chunk : " + rChunk.getChunk().getX() + "," + rChunk.getChunk().getZ() + " on world: " + rChunk.getWorldName());
                    if (RegeneratorPlugin.config.debugMode) e.printStackTrace();
                }
            } else {
                if (event.isImmediate()) {
                    RegeneratorPlugin.utils.throwMessage(MsgType.WARNING, "Queueing regeneration of chunk: " + rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.worldName + " as immediate regeneration was not possible due to the current TPS of the server.");
                    RegeneratorPlugin.utils.throwMessage(MsgType.WARNING, "You can disable this feature by setting 'regenerateUninitialisedChunksNearPlayersInstant' to false in global.yml");
                }
                if (RegeneratorPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk())) {
                    RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Updating activity of chunk chunk: " + rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.worldName);
                    rChunk.updateActivity();
                } else {
                    RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Not updating activity of chunk: " + rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.worldName + " as auto regen requirements are not met.");
                }
            }
        }
        if (!RegeneratorPlugin.utils.autoRegenRequirementsMet(event.getBlock().getChunk()) && rChunk.lastActivity != 0) {
            if (RegeneratorPlugin.config.debugMode) RegeneratorPlugin.utils.throwMessage(MsgType.DEBUG, "Resetting activity of chunk: " + rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.worldName + " as auto regen requirements are no longer met.");
            rChunk.resetActivity();
        }
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
