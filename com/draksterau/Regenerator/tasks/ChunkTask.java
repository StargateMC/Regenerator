/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.RegeneratorPlugin;
import com.draksterau.Regenerator.config.worldConfigHandler;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class ChunkTask extends BukkitRunnable {
    
    Chunk chunk;
    Plugin plugin;
    private Logger log = Logger.getLogger("Minecraft");
    
    public ChunkTask (Plugin plugin, Chunk chunk) {
        this.plugin = plugin;
        this.chunk = chunk;
    }
    
    
    
    @Override
    
    public void run() {   
            RegeneratorPlugin actualPlugin = (RegeneratorPlugin) plugin;

            if (chunk.getWorld().regenerateChunk(chunk.getX(),chunk.getZ())) {
              //  log.log(Level.INFO, "Chunk regenerated successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk regeneration failed for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            }
            worldConfigHandler wConfig = new worldConfigHandler((RegeneratorPlugin) plugin,chunk.getWorld());
            if (wConfig.shouldPopulate()) {
                        Random random = new Random(chunk.getWorld().getSeed());
                        // The below code is for testing if a chunk is needing to be populated.
                        long xRand = random.nextLong() / 2 * 2 + 1;
                        long zRand = random.nextLong() / 2 * 2 + 1;
                        random.setSeed((long) chunk.getX() * xRand + (long) chunk.getZ() * zRand ^ chunk.getWorld().getSeed());

                // The below code populates a chunk.
                for (BlockPopulator pop : chunk.getWorld().getPopulators()) {
                    pop.populate(chunk.getWorld(),random, chunk);
                }
            }
            actualPlugin.tellAllNotified(ChatColor.GRAY + "Chunk regenerating at: " + ChatColor.BLUE + chunk.getX()*16 + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ()*16 + ChatColor.GRAY + " on world: " + ChatColor.GREEN + chunk.getWorld().getName());
            if (chunk.getWorld().refreshChunk(chunk.getX(),chunk.getZ())) {
           //     log.log(Level.INFO, "Chunk refreshed successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            } else {
                log.log(Level.SEVERE, "Chunk refreshed failed for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
            }
    }
}

