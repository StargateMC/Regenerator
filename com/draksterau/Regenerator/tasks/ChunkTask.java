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

            if (RChunk.chunk.getWorld().regenerateChunk(RChunk.chunk.getX(),RChunk.chunk.getZ())) {
              //  log.log(Level.INFO, "Chunk regenerated successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk regeneration failed for chunk: {0},{1} on world: {2}", new Object[]{RChunk.chunk.getX(), RChunk.chunk.getZ(), RChunk.chunk.getWorld().getName()});
            }
            
            Random random = new Random(RChunk.chunk.getWorld().getSeed());
            // The below code is for testing if a chunk is needing to be populated.
            long xRand = random.nextLong() / 2 * 2 + 1;
            long zRand = (long) (random() / 2 * 2 + 1);
            random.setSeed((long) RChunk.chunk.getX() * xRand + (long) RChunk.chunk.getZ() * zRand ^ RChunk.chunk.getWorld().getSeed());

            // The below code populates a chunk.
            for (BlockPopulator pop : RChunk.chunk.getWorld().getPopulators()) {
                pop.populate(RChunk.chunk.getWorld(),random, RChunk.chunk);
            }
            RChunk.plugin.utils.tellAllNotified(ChatColor.GRAY + "Chunk regenerating at: " + ChatColor.BLUE + RChunk.chunk.getX()*16 + ChatColor.GRAY + "," + ChatColor.BLUE + RChunk.chunk.getZ()*16 + ChatColor.GRAY + " on world: " + ChatColor.GREEN + RChunk.chunk.getWorld().getName());
            if (RChunk.chunk.getWorld().refreshChunk(RChunk.chunk.getX(),RChunk.chunk.getZ())) {
           //     log.log(Level.INFO, "Chunk refreshed successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk refreshed failed for chunk: {0},{1} on world: {2}", new Object[]{RChunk.chunk.getX(), RChunk.chunk.getZ(), RChunk.chunk.getWorld().getName()});
            }
    }
}

