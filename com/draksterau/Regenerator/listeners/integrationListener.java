/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import com.draksterau.Regenerator.RegeneratorPlugin;
import java.util.List;
import org.bukkit.Bukkit;

/**
 *
 * @author draks
 */
public class integrationListener implements Listener {
    
    private final RegeneratorPlugin RegeneratorPlugin;
    
    public integrationListener(RegeneratorPlugin RegeneratorPlugin) {
        this.RegeneratorPlugin = RegeneratorPlugin;
    }
    

    // This listens for other plugins being enabled.
    
    public void onPluginEnable(PluginEnableEvent event) {
        if (RegeneratorPlugin.convertToModule(event.getPlugin().getName()) != null) {
            RegeneratorPlugin.loadIntegrationFor(RegeneratorPlugin.convertToModule(event.getPlugin().getName()));
        }        
    }
    
    public void onPluginDisable(PluginDisableEvent event) {
        if (RegeneratorPlugin.convertToModule(event.getPlugin().getName()) != null) {
            RegeneratorPlugin.disableIntegrationFor(RegeneratorPlugin.convertToModule(event.getPlugin().getName()));
        }        
    }
}
