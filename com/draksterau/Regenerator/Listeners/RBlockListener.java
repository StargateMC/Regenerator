/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Drakster
 */
public class RBlockListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        SRChunk sChunk = new SRChunk(e.getBlock().getChunk().getX(), e.getBlock().getChunk().getZ(), e.getBlock().getWorld().getName());
        if (!sChunk.isClaimed()) sChunk.updateActivity();
    }
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        SRChunk sChunk = new SRChunk(e.getBlock().getChunk().getX(), e.getBlock().getChunk().getZ(), e.getBlock().getWorld().getName());
        if (!sChunk.isClaimed()) sChunk.updateActivity();
    }
}
