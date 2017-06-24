/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.event;

import org.bukkit.Location;

/**
 *
 * @author draks
 */
public class RegenerationActionEvent extends RegenerationEvent {
    
    public RegenerationActionEvent(Location location) {
        super(location);
    }    
}