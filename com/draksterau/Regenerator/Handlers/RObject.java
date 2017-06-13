/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author draks
 */
abstract class RObject {
    
    public File configFile;
    public FileConfiguration config;
    
    // Access to the plugin.
    public RegeneratorPlugin plugin;
    
    public RObject(RegeneratorPlugin plugin) {
        this.plugin = plugin;
    }
    
    // This is the method that should handle saving for the object.
    abstract void loadData();
    // This is the method that should handle saving for the object.
    abstract void saveData();
    
}
