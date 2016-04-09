/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.threads;

import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import com.draksterau.Regenerator.tasks.RegenTask;
import com.draksterau.Regenerator.tasks.WorldTask;
import java.util.ArrayList;
import java.util.List;
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
    private int offset = 5;
    private Plugin plugin;
    private Logger log = Logger.getLogger("Minecraft");
    List<Thread> activeWorldThreads = new ArrayList<Thread>();
    
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
        for (World world : Bukkit.getWorlds()) {
            if (shouldRegenerateWorld(world)) {
                int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new WorldTask(plugin, world), offset * 20, plugin.getConfig().getInt("general.regeneration.worlds.interval") * 20);
                offset = offset + plugin.getConfig().getInt("general.regeneration.worlds.offset");
            } else {
                log.log(Level.INFO, "World: {0} is being skipped during regeneration process, as it is not valid in the config file.", world.getName());
            }
        }
    }
}
