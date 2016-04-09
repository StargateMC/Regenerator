/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.threads;

import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author draks
 */
public class ChunkThread extends Thread {
    
    Plugin plugin;
    Chunk chunk;
    private Logger log = Logger.getLogger("Minecraft");
    
    public ChunkThread (Plugin plugin, Chunk chunk) {
        this.plugin = plugin;
        this.chunk = chunk;
    }
    
    public boolean shouldRegenerate (Chunk chunk) {
        if (!plugin.getConfig().getBoolean("general.regeneration.enabled") == true) {
            log.info("Skipping chunk regeneration as it is disabled globally.");
            return false;
        }
        if (plugin.getConfig().getBoolean("integration.regeneration.factions.enabled") == true && !plugin.getConfig().getStringList("integration.regeneration.factions.definedFactions").contains(factionsIntegration.getFactionForChunk(chunk).getName())) {
            //log.info("Skipping chunk regeneration as the factions territory this chunk resides in is not to be regenerated.");
            return false;
        }
        if (onlinePlayersInChunk(chunk) > 0) {
            log.info("Skipping chunk regeneration as a player is online and in the chunk!");
            return false;
        }
        return true;
    }
    
    public void movePlayers(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!player.isOnline()) {
                    log.log(Level.INFO, "Moving Offline player:{0} to the spawn of world: {1}", new Object[]{player.getName(), chunk.getWorld().getName()});
                    player.teleport(chunk.getWorld().getSpawnLocation());
                }
            }
        }
    }
    
    public int onlinePlayersInChunk(Chunk chunk) {
        int count = 0;
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isOnline()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Override
    
    public void start() {             
        boolean regenFailed = false;
        while (Bukkit.getServer().getWorld(chunk.getWorld().getName()) instanceof World) {
            if (shouldRegenerate(chunk)) {
                    if (plugin.getConfig().getBoolean("general.generation.worlds.teleportOfflineInChunkToWorldSpawn")) {
                        movePlayers(chunk);
                    }
                    if (chunk.getWorld().regenerateChunk(chunk.getX(),chunk.getZ())) {
                        //log.log(Level.INFO, "Chunk regenerated successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                    } else {
                        log.log(Level.SEVERE, "Chunk regeneration failed for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                        regenFailed = true;
                    }

                    // The below code is for testing if a chunk is needing to be populated.
                    Random random = new Random(chunk.getWorld().getSeed());
                    long xRand = random.nextLong() / 2 * 2 + 1;
                    long zRand = random.nextLong() / 2 * 2 + 1;
                    random.setSeed((long) chunk.getX() * xRand + (long) chunk.getZ() * zRand ^ chunk.getWorld().getSeed());

                    // The below code populates a chunk.
                    for (BlockPopulator pop : chunk.getWorld().getPopulators()) {
                        pop.populate(chunk.getWorld(),random, chunk);
                    }

                    if (chunk.getWorld().refreshChunk(chunk.getX(),chunk.getZ())) {
                        //log.log(Level.INFO, "Chunk refreshed successfully for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                    } else {
                        log.log(Level.SEVERE, "Chunk refreshed failed for chunk: {0},{1} on world: {2}", new Object[]{chunk.getX(), chunk.getZ(), chunk.getWorld().getName()});
                        regenFailed = true;
                    }

                    if (regenFailed == false) {
                        break;
                    }
                    try {
                        Thread.sleep(plugin.getConfig().getInt("general.regeneration.worlds.chunks.interval") * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WorldThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
    }
}
