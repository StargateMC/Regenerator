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
public class WorldThread extends Thread {
    private boolean loop = false;
    private int loopTimes = 0;
    private Plugin plugin;
    private Logger log = Logger.getLogger("Minecraft");
    private World world;

    public WorldThread(Plugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }
   
    public boolean shouldRegenerate (Chunk chunk) {
        if (!plugin.getConfig().getBoolean("general.regeneration.enabled") == true) {
            log.info("Skipping chunk regeneration as it is disabled globally.");
            return false;
        }
        if (!plugin.getConfig().getStringList("general.regeneration.worlds.definedWorlds").contains(chunk.getWorld().getName()) && !plugin.getConfig().getBoolean("general.regeneration.worlds.isBlacklist") == true) {
            log.info("Skipping chunk regeneration as the world in question is not on the whitelist.");
            return false;
        }
        if (plugin.getConfig().getStringList("general.regeneration.worlds.definedWorlds").contains(chunk.getWorld().getName()) && plugin.getConfig().getBoolean("general.regeneration.worlds.isBlacklist") == true) {
            log.info("Skipping chunk regeneration as the world in question is on the blacklist.");
            return false;
        }
        if (plugin.getConfig().getBoolean("integration.regeneration.factions.enabled") == true && !plugin.getConfig().getStringList("integration.regeneration.factions.definedFactions").contains(factionsIntegration.getFactionForChunk(chunk).getName())) {
            log.info("Skipping chunk regeneration as the factions territory this chunk resides in is not to be regenerated.");
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
                    log.info("Moving Offline player:" + player.getName() + " to the spawn of world: " + chunk.getWorld().getName());
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
        log.log(Level.INFO, "Loading World: {0}", world.getName());
        for (Chunk chunk : world.getLoadedChunks()) {
            if (shouldRegenerate(chunk)) {
                if (plugin.getConfig().getBoolean("general.generation.worlds.teleportOfflineInChunkToWorldSpawn")) {
                    movePlayers(chunk);
                }
                if (chunk.getWorld().regenerateChunk(chunk.getX(),chunk.getZ())) {
                 log.info("Chunk regenerated successfully for chunk: " + chunk.getX() + "," + chunk.getZ() + " on world: " + chunk.getWorld().getName());   
                } else {
                 log.info("Chunk regeneration failed for chunk: " + chunk.getX() + "," + chunk.getZ() + " on world: " + chunk.getWorld().getName());
                }
                
                // The below code is for testing if a chunk is needing to be populated.
                Random random = new Random(world.getSeed());
                long xRand = random.nextLong() / 2 * 2 + 1;
                long zRand = random.nextLong() / 2 * 2 + 1;
                random.setSeed((long) chunk.getX() * xRand + (long) chunk.getZ() * zRand ^ world.getSeed());
                
                // The below code populates a chunk.
                for (BlockPopulator pop : chunk.getWorld().getPopulators()) {
                    pop.populate(chunk.getWorld(),random, chunk);
                }
                
                if (chunk.getWorld().refreshChunk(chunk.getX(),chunk.getZ())) {
                 log.info("Chunk refreshed successfully for chunk: " + chunk.getX() + "," + chunk.getZ() + " on world: " + chunk.getWorld().getName());   
                } else {
                 log.info("Chunk refreshed failed for chunk: " + chunk.getX() + "," + chunk.getZ() + " on world: " + chunk.getWorld().getName());
                }
                
                try {
                    Thread.sleep(plugin.getConfig().getInt("general.regeneration.interval") * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(WorldThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        log.log(Level.INFO, "Finished with world: {0}", world.getName());
    }

}
