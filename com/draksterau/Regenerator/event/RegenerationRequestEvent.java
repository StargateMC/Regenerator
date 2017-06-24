/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author draks
 */
public class RegenerationRequestEvent extends RegenerationEvent {

    private final Player requestor;
    private RequestTrigger trigger = RequestTrigger.Unknown;
    private boolean performImmediately = false;
    private JavaPlugin plugin;
    
    public RegenerationRequestEvent(Location location, Player player, RequestTrigger trigger, JavaPlugin requestingPlugin) {
        super(location);
        this.requestor = player;
        this.trigger = trigger;
        this.plugin = requestingPlugin;
    }
    
    public JavaPlugin getPluginRequestor() {
        return this.plugin;
    }
    
    public boolean isImmediate() {
        return this.performImmediately;
    }
    
    public void setIsImmediate(boolean val) {
        this.performImmediately = val;
    }
    
    public RequestTrigger getTrigger() {
        return this.trigger;
    }
    
    public Player getRequestor() {
        return this.requestor;
    }
}