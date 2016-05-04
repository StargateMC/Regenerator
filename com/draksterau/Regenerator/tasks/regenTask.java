/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.RegeneratorPlugin;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class regenTask extends BukkitRunnable {
    
    RegeneratorPlugin plugin;
    
    private Logger log = Logger.getLogger("Minecraft");
    
    public regenTask (RegeneratorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (RWorld RWorld : plugin.loadedWorlds) {
            // Do stuff.
        }
    }
}
