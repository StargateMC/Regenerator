/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator;

import com.draksterau.Regenerator.Utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Drakster
 */

public class Regenerator extends JavaPlugin {
    
    Utils utils;
    public static Regenerator instance;
    
    @Override
    public void onEnable() {
        
        instance = this;
    }
    
    public Utils getUtils() {
        if (utils == null) utils = new Utils();
        return utils;
    }
    
    public static Regenerator getInstance() {
        return Regenerator.instance;
    }
}
