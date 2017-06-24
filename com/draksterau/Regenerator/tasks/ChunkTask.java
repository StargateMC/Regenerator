/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

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
            if (!isManual) {
                // Lets check if the world the chunk is on is loaded still.
                if (Bukkit.getServer().getWorld(RChunk.worldName) == null) {
                    RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". The world was unloaded and will regenerate next time it is found!");
                    return;
                }
                
                // Now fires an action event for other plugins to cancel.
                
                // Creating fake location for the regeneration event. For future region based support.
                Location location = new Location(this.RChunk.getWorld(), (RChunk.chunkX*16), 0.0, (RChunk.chunkZ*16));
                RegenerationActionEvent actionEvent = new RegenerationActionEvent(location);
                
                Bukkit.getServer().getPluginManager().callEvent(actionEvent);
                if (actionEvent.isCancelled()) {
                    int reasonCount = 1;                                            
                    RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ", for the following reason(s):");
                    for (String s : actionEvent.getCancelledReasons().keySet()) {
                        RChunk.plugin.utils.throwMessage("info", "Skip reason " + reasonCount + ": " + s + " provided by : " + actionEvent.getCancelledReasons().get(s).getName() + ".");
                        reasonCount++;
                    }
                    RChunk.resetActivity();
                    return;        
                }
                
                // Now checking if a chunk is claimed at the point of regenerating only.
                if (!RChunk.plugin.utils.autoRegenRequirementsMet(RChunk.getChunk())) {
                    RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". It most likely was claimed?");
                    RChunk.resetActivity();
                    return;
                }
                if (!RChunk.plugin.utils.isLagOK()) {
                    RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". TPS is below that defined in global configuration.");
                    return;
                }
                if (!RChunk.plugin.utils.getPlayersNearChunk(RChunk, RChunk.plugin.config.distanceNearbyMinimum).isEmpty()) {
                   RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". There are players closer than " + RChunk.plugin.config.distanceNearbyMinimum + " blocks away.");
                   return;
                }
                if (RChunk.getChunk().isLoaded() && !RChunk.plugin.config.targetLoadedChunks) {
                   RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". Loaded chunks are disabled for auto regeneration in global configuration.");
                   return;
                }
                if (!RChunk.getChunk().isLoaded() && !RChunk.plugin.config.targetUnloadedChunks) {
                   RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". Unloaded chunks are disabled for auto regeneration in global configuration.");
                   return;
                }
                if (RChunk.getChunk().getWorld().isChunkInUse(RChunk.chunkX, RChunk.chunkZ) && !RChunk.plugin.config.regenerateChunksInUseByPlayers) {
                   RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". One or more players are using this chunk (Players can be ignored in config).");
                   return;
                }
                if (RChunk.plugin.config.warpDriveCompatibility && RChunk.plugin.utils.isWarpCoreNearby(RChunk.getChunk()) != null) {
                   RChunk.plugin.utils.throwMessage("info", "Skipping regeneration of chunk: " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName + ". A WarpDrive ship core has been detected too close to this chunk.");
                   return;
                }
            }
            if (!RChunk.getChunk().isLoaded()) {
                RChunk.plugin.utils.throwMessage("info", "Loading chunk to regenerate!");
                RChunk.getChunk().load();
                wasUnloaded = true;
            }
            
            RChunk.plugin.utils.throwMessage("info","Regenerating : " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName);
            
            if (RChunk.plugin.config.clearRegeneratedChunksOfEntities) {
                RChunk.plugin.utils.throwMessage("info", "Clearing entities from chunk...");
                RChunk.plugin.utils.clearEntitiesFromChunk(RChunk);
            }
            
            if (RChunk.getWorld().regenerateChunk(RChunk.chunkX,RChunk.chunkZ)) {
              //  log.log(Level.INFO, "Chunk regenerated successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk regeneration failed for chunk: {0},{1} on world: {2}", new Object[]{RChunk.chunkX, RChunk.chunkZ, RChunk.getWorld().getName()});
            }
            
            Random random = new Random(RChunk.getChunk().getWorld().getSeed());
            // The below code is for testing if a chunk is needing to be populated.
            long xRand = random.nextLong() / 2 * 2 + 1;
            long zRand = (long) (random() / 2 * 2 + 1);
            random.setSeed((long) RChunk.chunkX * xRand + (long) RChunk.chunkZ * zRand ^ RChunk.getWorld().getSeed());

            // The below code populates a chunk.
            for (BlockPopulator pop : RChunk.getWorld().getPopulators()) {
                pop.populate(RChunk.getWorld(),random, RChunk.getChunk());
            }
            
            if (RChunk.getWorld().refreshChunk(RChunk.chunkX,RChunk.chunkZ)) {
           //     log.log(Level.INFO, "Chunk refreshed successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk refreshed failed for chunk: {0},{1} on world: {2}", new Object[]{RChunk.chunkX, RChunk.chunkZ, RChunk.getWorldName()});
            }
            RChunk.resetActivity();
            if (wasUnloaded) {
                RChunk.plugin.utils.throwMessage("info", "Unloading regenerated chunk, as it was only loaded to regenerate!");
                RChunk.getChunk().unload();
            }
    }
}

