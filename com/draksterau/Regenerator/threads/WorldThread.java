/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.threads;

import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import com.draksterau.Regenerator.tasks.ChunkTask;
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
    
    @Override
    public void start() {
        while (Bukkit.getServer().getWorld(world.getName()) instanceof World) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ChunkTask(plugin, chunk));
                while (Bukkit.getServer().getScheduler().isCurrentlyRunning(id) || Bukkit.getServer().getScheduler().isQueued(id)) {
                    //log.log(Level.INFO, "Waiting on Chunk task: {0} for world: " + world.getName(), id);
                    if (!(Bukkit.getServer().getWorld(world.getName()) instanceof World)) {
                        break;
                    }
                }
            }
            break;
        }
    }

}
