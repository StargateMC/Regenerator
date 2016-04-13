/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.tasks;

import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author draks
 */
public class lagTask extends BukkitRunnable {

    public static int tickCount = 0;
    public static long[] TICKS= new long[600];
    public static long LAST_TICK= 0L;
    
    public static double getTps() {
        return getTPS(100);
    }
    
    public static double getTPS(int ticks) {
        if (tickCount < ticks) {
            return 20.0D;
        }
        int target = (tickCount - 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];
        
        return ticks / (elapsed / 1000.0D);
    }
   
    public static long getElapsed(int tickID) {
   
        if (tickCount - tickID >= TICKS.length) {
        }

        long time = TICKS[(tickID % TICKS.length)];
        return System.currentTimeMillis() - time;
        
    }
    
    @Override
    public void run() {
        TICKS[(tickCount% TICKS.length)] = System.currentTimeMillis();
        tickCount += 1;
    }
    
}
