/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.Handlers.RChunk;
import static java.lang.Math.random;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class ChunkTask extends BukkitRunnable {
    
    RChunk RChunk;
    private Logger log = Logger.getLogger("Minecraft");
    
    public ChunkTask (RChunk RChunk) {
        this.RChunk = RChunk;
    }
    
    
    
    @Override
    
    public void run() {   
            RChunk.plugin.utils.throwMessage("info","Regenerating : " + RChunk.chunkX + "," + RChunk.chunkZ + " on world: " + RChunk.worldName);
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
    }
}

