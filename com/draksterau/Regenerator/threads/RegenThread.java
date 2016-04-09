/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.threads;

import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author draks
 */
public class RegenThread extends Thread {
    private boolean loop = true;
    private int loopTimes = 0;
    private Plugin plugin;
    private Logger log = Logger.getLogger("Minecraft");

    public RegenThread(Plugin plugin) {
        this.plugin = plugin;
    }
    // This is used to determine if we should regenerate or not.


    
    public static void movePlayers(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!player.isOnline()) {
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
    
    public boolean shouldRegenerateWorld(World world) {
        if (!plugin.getConfig().getStringList("general.regeneration.worlds.definedWorlds").contains(world.getName()) && !plugin.getConfig().getBoolean("general.regeneration.worlds.isBlacklist") == true) {
            log.info("Skipping chunk regeneration as the world in question is not on the whitelist.");
            return false;
        }
        if (plugin.getConfig().getStringList("general.regeneration.worlds.definedWorlds").contains(world.getName()) && plugin.getConfig().getBoolean("general.regeneration.worlds.isBlacklist") == true) {
            log.info("Skipping chunk regeneration as the world in question is on the blacklist.");
            return false;
        }
        return true;
    }
    
    @Override
    public void start() {
        while (loop == true) {
            for (World world : Bukkit.getWorlds()) {
                if (shouldRegenerateWorld(world)) {
                    Thread worldThread = new WorldThread(plugin, world);
                    worldThread.start();
                    try {
                        worldThread.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RegenThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    log.info("World: " + world.getName() + " is being skipped during regeneration process, as it is not valid in the config file.");
                }
            }
        }
    }
}
