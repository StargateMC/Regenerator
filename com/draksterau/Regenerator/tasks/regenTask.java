/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import com.draksterau.Regenerator.Handlers.MsgType;
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
        
        this.plugin.isParseActive = true;
        
        if (!this.plugin.utils.isLagOK()) {
            plugin.utils.throwMessage(MsgType.WARNING, String.format(plugin.lang.getForKey("messages.regenPausedTPSLow"),plugin.config.minTpsRegen));
            this.plugin.isParseActive = false;
            return;
        }
        
        this.plugin.chunksToRegenCached = getNumChunksToRegen().size();
        if (this.plugin.chunksToRegenCached > 0) plugin.utils.throwMessage(MsgType.INFO, String.format(plugin.lang.getForKey("messages.totalChunksToRegen"),this.plugin.chunksToRegenCached));
        if (this.plugin.isBacklogged()) plugin.utils.throwMessage(MsgType.WARNING,"Regeneration queue is currently backlogged. It will catch up in approximately " + plugin.utils.getEnglishTimeFromMs((long)Math.floor(plugin.parseQueue() * plugin.config.parseInterval)*1000) + " (" + Math.ceil(plugin.parseQueue()) + " executions of the Regeneration Task).");

        numWorlds = 0;
        chunksToRegenerate.clear();
        offsetTicks = 0;
        secsBetweenChunks = 0;
        numChunks = 0;
        secsTotal = 0;
        
        plugin.utils.throwMessage(MsgType.INFO, String.format(plugin.lang.getForKey("messages.regenParseStarting")));
        for (RWorld RWorld : plugin.loadedWorlds) {
            if (!getChunksToRegen(RWorld).isEmpty()) {
                numWorlds++;
            }
        }
        if (!chunksToRegenerate.isEmpty()) {
            numChunks = chunksToRegenerate.size() - 1;
            if (numChunks == 0) numChunks = 1;
            secsTotal = (plugin.config.parseInterval * plugin.config.percentIntervalRuntime);
            secsBetweenChunks = (secsTotal / numChunks);
            plugin.utils.throwMessage(MsgType.INFO, String.format(plugin.lang.getForKey("messages.regenParseStart"), Math.ceil(secsBetweenChunks), secsTotal));
            for (RChunk rChunk : chunksToRegenerate) {
                try {
                    new ChunkTask(rChunk, false).runTaskLater(plugin, (long)offsetTicks);
                    offsetTicks = offsetTicks + (secsBetweenChunks * 20);
                    plugin.utils.throwMessage(MsgType.DEBUG, "Queueing regeneration of : " +  rChunk.chunkX + "," + rChunk.chunkZ + " on world: " + rChunk.getWorldName() + " in " + offsetTicks + " ticks (" + (plugin.utils.getEnglishTimeFromMs((long)(offsetTicks/20) * 1000)) + ")");
                } catch (Exception e) {
                    plugin.utils.throwMessage(MsgType.SEVERE, String.format(plugin.lang.getForKey("messages.queueChunkTaskException"), rChunk.getChunk().getX(), rChunk.getChunk().getZ(), rChunk.getWorldName(), e.getMessage()));
                    if (plugin.config.debugMode) e.printStackTrace();
                }
                
            }

        }
        try {
            Thread.sleep(1000 + ((long)secsTotal * 1000) + 1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(regenTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (chunksToRegenerate.isEmpty()) {
            plugin.utils.throwMessage(MsgType.INFO, String.format(plugin.lang.getForKey("messages.regenTaskCompletedNothingDone")));
        }
        if (!chunksToRegenerate.isEmpty()) {
            plugin.utils.throwMessage(MsgType.INFO, String.format(plugin.lang.getForKey("messages.regenTaskCompleted"), numChunks, numWorlds));
        }
        plugin.isParseActive = false;
    }
    
    
    public List<RChunk> getChunksToRegen(RWorld rWorld) {
        List<RChunk> rChunks = rWorld.getAllRChunks();
        List<RChunk> chunksToRegen = new ArrayList<RChunk>();
        int count = 0;
        for (RChunk rChunk : rChunks) {
            if ((System.currentTimeMillis() - rChunk.lastActivity) >= (rWorld.regenInterval * 1000) && rChunk.lastActivity != 0 && rChunk.lastActivity != -1) {
                if (rChunk.canAutoRegen() && count <= plugin.config.numChunksPerParse) {
                    chunksToRegenerate.add(rChunk);
                    chunksToRegen.add(rChunk);
                    count++;
                }
            }
        }
        return chunksToRegen;
    }
    
    public List<RChunk> getNumChunksToRegen() {
        List<RChunk> chunksToRegen = new ArrayList<RChunk>();
        for (RWorld rWorld : plugin.loadedWorlds) {
            List<RChunk> rChunks = rWorld.getAllRChunks();
            for (RChunk rChunk : rChunks) {
                if ((System.currentTimeMillis() - rChunk.lastActivity) >= (rWorld.regenInterval * 1000) && rChunk.lastActivity != 0 && rChunk.lastActivity != -1) {
                    if (rChunk.canAutoRegen()) {
                        chunksToRegen.add(rChunk);
                    }
                }
            }
        }
        return chunksToRegen;
    }
}
