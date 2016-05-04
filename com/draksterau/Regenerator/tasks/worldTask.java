/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.worlds.RWorld;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class worldTask extends BukkitRunnable {
    
    RWorld RWorld;
    
    private Logger log = Logger.getLogger("Minecraft");
    
    public worldTask (RWorld RWorld) {
        this.RWorld = RWorld;
    }
    
    @Override
    public void run() {
        if (RWorld.getNumChunks() > 0 && RWorld.plugin.loadedWorlds.contains(RWorld)) {
            RWorld.plugin.throwMessage("info", "Beginning world task for: " + RWorld.world.getName() + " (" + RWorld.getNumChunks() + " chunks, Interval between chunks: " + RWorld.getIntervalSecs() + " sec, " + (RWorld.getNumChunks() * RWorld.getIntervalMins()) + " mins to complete)");
            
        } else {
            RWorld.plugin.throwMessage("info", "World has been unloaded: " + RWorld.world.getName() + ", terminating task!");
            this.cancel();
        }
    }
}
