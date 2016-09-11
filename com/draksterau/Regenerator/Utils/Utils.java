/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Utils;

import com.draksterau.Regenerator.Regenerator;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getConsoleSender;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Drakster
 */
public class Utils {
    
    LangUtils langUtils;
    
    public void throwMessage(JavaPlugin plugin, String type, String message) {
        
        switch (type) {
            case "info":
                getConsoleSender().sendMessage(formatName(plugin.getName()) + ChatColor.DARK_AQUA + "[" + type.toUpperCase() + "]: " + message);
                break;
            case "warning":
                getConsoleSender().sendMessage(formatName(plugin.getName()) + ChatColor.DARK_AQUA + "[" + type.toUpperCase() + "]: " + message);
                break;
            case "severe":
                getConsoleSender().sendMessage(formatName(plugin.getName()) + ChatColor.RED + "[" + type.toUpperCase() + "]: " + message);
                disablePlugin(Regenerator.getInstance());
                break;
            case "new": 
                getConsoleSender().sendMessage(formatName(plugin.getName()) + ChatColor.LIGHT_PURPLE + "[" + type.toUpperCase() + "]: " + message);
                break;
            case "success":
                getConsoleSender().sendMessage(formatName(plugin.getName()) + ChatColor.GREEN + "[" + type.toUpperCase() + "]: " + message);
                break;
            default:
                throwMessage(plugin, "severe", "Fatal throwMessage call by plugin: " + plugin.getName() + " with message: " + message);
                break;
        }
    }
    
    public ConsoleCommandSender getConsoleSender() {
        return Bukkit.getServer().getConsoleSender();
    }
    // Disables the specified plugin.
    public void disablePlugin(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }
    // Gets the lang-i-fied version of the plugin name.
    public String getLangPluginName(JavaPlugin plugin) {
        return formatName(Regenerator.getInstance().getUtils().getLangUtils(plugin).getForKey("messages.pluginName"));
    }
    // Returns a formatted version of the plugins name.
    public String formatName(String pluginName) {
        return ChatColor.RED + "[" + ChatColor.DARK_GREEN + pluginName + ChatColor.RED + "] " + ChatColor.GRAY;
    }

    public LangUtils getLangUtils(JavaPlugin plugin) {
        // Load Utils if they are not set yet.
        if (langUtils == null) langUtils = new LangUtils(plugin);
        // Return the instance.
        return langUtils;
    }
}
