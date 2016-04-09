/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.threads.WorldThread;
import com.draksterau.Regenerator.threads.ChunkThread;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class ChunkTask extends BukkitRunnable {
    
    Plugin plugin;
    Chunk chunk;
    private Logger log = Logger.getLogger("Minecraft");


    public ChunkTask (Plugin plugin, Chunk chunk) {
        this.plugin = plugin;
        this.chunk = chunk;
    }

    
    @Override
    public void run() {     
        if (Bukkit.getServer().getWorld(chunk.getWorld().getName()) instanceof World) {
            try {
                log.log(Level.INFO, "Starting Regeneration for Chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                Random random = new Random();
                int randomInt = random.nextInt(10);
                try {
                    Thread.sleep(randomInt * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChunkTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                Thread ChunkThread = new ChunkThread(plugin, chunk);
                ChunkThread.start();
                ChunkThread.join();
                log.log(Level.INFO, "Finished Regeneration for Chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                Thread.sleep(plugin.getConfig().getInt("general.regeneration.worlds.chunks.interval") * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(WorldTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.log(Level.WARNING, "Regeneration failed for chunk: {0},{1} on world : {2} is no longer loaded.", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
        }
    }	
}

