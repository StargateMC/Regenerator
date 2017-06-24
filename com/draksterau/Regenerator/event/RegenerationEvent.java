/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.event;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author draks
 */
public class RegenerationEvent extends Event {
    
    private Location location;
    private boolean cancelled = false;
    private HashMap<String, JavaPlugin> reasons = new HashMap<>();
    private static final HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlerList() {
	return handlers;
    }
    
    public RegenerationEvent(Location location) {
        this.location = location;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public Chunk getChunk() {
        return location.getChunk();
    }
    
    public Block getBlock() {
        return location.getBlock();
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public World getWorld() {
        return location.getWorld();
    }
    
    public void clearCancellations() {
        this.reasons.clear();
        this.cancelled = false;
    }
    
    public HashMap<String, JavaPlugin> getCancelledReasons() {
        return this.reasons;
    }
    
    public void cancelWithReason(String reason, JavaPlugin yourPlugin) {
        if (!this.cancelled) this.cancelled = true;
        if (!this.reasons.keySet().contains(reason)) this.reasons.put(reason, yourPlugin);        
    }
    
}



