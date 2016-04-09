/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.threads.WorldThread;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class WorldTask extends BukkitRunnable {
    
    Plugin plugin;
    World world;
    
    private Logger log = Logger.getLogger("Minecraft");
    
    // This is used to determine if we should regenerate or not.

    public WorldTask (Plugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    
    @Override
    public void run() {        
        log.log(Level.INFO, "Starting Regeneration for World: {0}", world.getName());
        if (Bukkit.getServer().getWorld(world.getName()) instanceof World) {
            try {
                Thread WorldThread = new WorldThread(plugin, world);
                WorldThread.start();
                WorldThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(WorldTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.log(Level.INFO, "Finished Regeneration for World: {0}", world.getName());
        } else {
            log.log(Level.WARNING, "Regeneration failed as world : {0} is no longer loaded.", world.getName());
        }
    }	
}

