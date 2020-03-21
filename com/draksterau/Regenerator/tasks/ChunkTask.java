/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.Handlers.MsgType;
import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.event.RegenerationActionEvent;
import static java.lang.Math.random;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class ChunkTask extends BukkitRunnable {
    
    private boolean wasUnloaded;
    RChunk RChunk;
    private Logger log = Logger.getLogger("Minecraft");
    private boolean isManual;
    
    public ChunkTask (RChunk RChunk, boolean isManual) {
        this.RChunk = RChunk;
        this.isManual = isManual;
    }
    
    
    
    @Override
    
    public void run() {   
            RChunk.plugin.utils.throwMessage(MsgType.DEBUG, "Beginning chunk regeneration task for : " + RChunk.chunkX + "," + RChunk.chunkZ + " on world : " + RChunk.getWorldName() + "...");
            if (!isManual) {
                // Lets check if the world the chunk is on is loaded still.
                if (Bukkit.getServer().getWorld(RChunk.worldName) == null) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenWorldUnloaded"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                    return;
                }
                
                // Now fires an action event for other plugins to cancel.
                
                // Creating fake location for the regeneration event. For future region based support.
                Location location = new Location(this.RChunk.getWorld(), (RChunk.chunkX*16), 0.0, (RChunk.chunkZ*16));
                RegenerationActionEvent actionEvent = new RegenerationActionEvent(location);
                
                Bukkit.getServer().getPluginManager().callEvent(actionEvent);
                if (actionEvent.isCancelled()) {
                    int reasonCount = 1;                                            
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenDueToActionEventCancellation"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                    for (String s : actionEvent.getCancelledReasons().keySet()) {
                        RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenActionEventResult"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName(), reasonCount, s, actionEvent.getCancelledReasons().get(s).getName()));
                        reasonCount++;
                    }
                    RChunk.resetActivity();
                    return;        
                }
                if (RChunk.plugin.config.enableUnknownProtectionDetection && !RChunk.plugin.utils.canBreakChunk(RChunk.getChunk())) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenProtectedUnknownSource"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                    RChunk.resetActivity();
                    return;
                }
                // Now checking if a chunk is claimed at the point of regenerating only.
                if (!RChunk.plugin.utils.autoRegenRequirementsMet(RChunk.getChunk())) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenAutoRegenRequirementFail"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName(),RChunk.plugin.config.distanceNearbyMinimum));
                    RChunk.resetActivity();
                    return;
                }
                if (!RChunk.plugin.utils.isLagOK()) {
                    RChunk.plugin.utils.throwMessage(MsgType.WARNING, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenTPSToLow"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName(),RChunk.plugin.config.minTpsRegen));
                    return;
                }
                if (!RChunk.plugin.utils.getPlayersNearChunk(RChunk, RChunk.plugin.config.distanceNearbyMinimum).isEmpty()) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.distanceNearbyMinimum"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName(),RChunk.plugin.config.distanceNearbyMinimum));
                    return;
                }
                if (RChunk.getChunk().isLoaded() && !RChunk.plugin.config.targetLoadedChunks) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenOfLoadedChunk"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                   return;
                }
                if (!RChunk.getChunk().isLoaded() && !RChunk.plugin.config.targetUnloadedChunks) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenOfUnloadedChunk"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                   return;
                }
                if (RChunk.getChunk().getWorld().isChunkInUse(RChunk.chunkX, RChunk.chunkZ) && !RChunk.plugin.config.regenerateChunksInUseByPlayers) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenOfChunkInUseByPlayers"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                    return;
                }
                if (RChunk.plugin.config.warpDriveCompatibility && RChunk.plugin.utils.isWarpCoreNearby(RChunk.getChunk()) != null) {
                    RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.skippingRegenOfChunkNearWarpCore"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                    return;
                }
            }
            if (!RChunk.getChunk().isLoaded()) {
            RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.loadingToRegen"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                RChunk.getChunk().load();
                wasUnloaded = true;
            }
            
            RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.startingRegenChunk"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
            
            if (RChunk.plugin.config.clearRegeneratedChunksOfEntities) {
                RChunk.plugin.utils.throwMessage(MsgType.DEBUG, String.format(RChunk.plugin.lang.getForKey("messages.regenClearEntities"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                RChunk.plugin.utils.clearEntitiesFromChunk(RChunk);
            }
            
            if (RChunk.plugin.utils.regenerateChunk(RChunk.getChunk())) {
                RChunk.plugin.utils.throwMessage(MsgType.SUCCESS, String.format(RChunk.plugin.lang.getForKey("messages.regenSuccess"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
            } else {
                RChunk.plugin.utils.throwMessage(MsgType.SEVERE, String.format(RChunk.plugin.lang.getForKey("messages.regenFailed"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
            }
            RChunk.resetActivity();
            if (wasUnloaded) {
                RChunk.plugin.utils.throwMessage(MsgType.INFO, String.format(RChunk.plugin.lang.getForKey("messages.unloadingChunkLoadedForRegen"), RChunk.getChunk().getX(), RChunk.getChunk().getZ(), RChunk.getWorldName()));
                RChunk.getChunk().unload();
            }
    }
}

