/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.RegeneratorPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class regenTask extends BukkitRunnable {
    
    RegeneratorPlugin plugin;

    List<RChunk> chunksToRegenerate = new ArrayList<RChunk>();
    
    double offsetTicks = 0;

    int numWorlds = 0;
    int numChunks = 0;
    double secsBetweenChunks = 0;
    double secsTotal = 0;
    
    List<Integer> taskIDs = new ArrayList<Integer>();
    
    private Logger log = Logger.getLogger("Minecraft");
    
    public regenTask (RegeneratorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        
        if (this.plugin.isPaused) {
            plugin.utils.throwMessage("info", "Regenerator is not beginning to parse inactive chunks as the TPS is below " + plugin.config.minTpsRegen + " (defined in global.yml).");
            return;
        }
        numWorlds = 0;
        chunksToRegenerate.clear();
        offsetTicks = 0;
        secsBetweenChunks = 0;
        numChunks = 0;
        secsTotal = 0;
        
        plugin.utils.throwMessage("info", "Regeneration task is starting...");
        for (RWorld RWorld : plugin.loadedWorlds) {
            if (!getChunksToRegen(RWorld).isEmpty()) {
                numWorlds++;
            }
        }
        if (!chunksToRegenerate.isEmpty()) {
            numChunks = chunksToRegenerate.size() - 1;
            secsTotal = (plugin.config.parseInterval * plugin.config.percentIntervalRuntime);
            secsBetweenChunks = (secsTotal / numChunks);
            plugin.utils.throwMessage("info", "Regenerator will regenerate 1 chunk per " + secsBetweenChunks + " seconds for " + secsTotal + " seconds.");
            for (RChunk rChunk : chunksToRegenerate) {
                Bukkit.getServer().getScheduler().runTaskLater(plugin, new ChunkTask(rChunk), (long)offsetTicks);
                offsetTicks = offsetTicks + (secsBetweenChunks * 20);
            }

        }
        try {
            Thread.sleep(1000 + ((long)secsTotal * 1000));
        } catch (InterruptedException ex) {
            Logger.getLogger(regenTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (chunksToRegenerate.isEmpty()) {
            plugin.utils.throwMessage("info", "Regeneration task completed without regenerating any chunks.");
        }
        if (!chunksToRegenerate.isEmpty()) {
            plugin.utils.throwMessage("info", "Regeneration task completed after processing " +  numChunks + " chunks on " + numWorlds + " worlds.");
        }
 
    }
    
    
    public List<RChunk> getChunksToRegen(RWorld rWorld) {
        List<RChunk> rChunks = rWorld.getAllRChunks();
        List<RChunk> chunksToRegen = new ArrayList<RChunk>();
        int count = 0;
        for (RChunk rChunk : rChunks) {
            if ((System.currentTimeMillis() - rChunk.lastActivity) >= (rWorld.regenInterval * 1000) && rChunk.lastActivity != 0) {
                if (rChunk.canAutoRegen() && count <= plugin.config.numChunksPerParse) {
                    chunksToRegenerate.add(rChunk);
                    chunksToRegen.add(rChunk);
                    count++;
                }
            }
        }
        return chunksToRegen;
    }
}
