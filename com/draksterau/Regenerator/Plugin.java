package com.draksterau.Regenerator;

import com.draksterau.Regenerator.tasks.RegenTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Plugin extends JavaPlugin {
    
    // Config gets loaded here in onEnable()
    static FileConfiguration config;
        
    private Logger log = Logger.getLogger("Minecraft");
    
    @Override
    public void onEnable () {
        log.info("Loaded Regenerator!");
        config = loadConfiguration();
        if (config.getBoolean("general.regeneration.enabled") == true) {
            log.info("Starting Regenerator v" + getConfig().getString("version") + "...");
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(this, new RegenTask(this));
        } else {
            this.disablePlugin();
        }
    }

    public FileConfiguration loadConfiguration() {
        saveDefaultConfig();   
        if (getConfig().get("version").equals(this.getDescription().getVersion())) {
            // Reloads configuration.
            log.info("Loading configuration....");
            reloadConfig();
        } else {
            log.info("Version mismatch between plugin and config file, version is: " + this.getDescription().getVersion());
            log.warning("Configuration load failed, resetting config file...");
            configure();
            saveConfig();
        }
        return getConfig();
    }
    
    public static void tellPlayers(Chunk chunk, String message) {
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isOnline()) {
                    player.sendMessage(message);
                }                
            }
        }
    }

    
    @Override
    public void onDisable () {
        log.info("Unloaded Regenerator!");
    }

    public void configure () {
        
        // Regenerator Version
        getConfig().set("version", "1.1.0");
        
        // Is Regeneration enabled?
        getConfig().set("general.regeneration.enabled", true);

        // Seconds between Regeneration sweeeps
        int interval = 15;
        getConfig().set("general.regeneration.interval", interval);

        // Teleport players who are still in a chunk to the world spawn?
        getConfig().set("general.regeneration.worlds.teleportOfflineInChunkToWorldSpawn", true);
        
        // List of worlds that have regeneration disabled
        List<String> definedWorlds = Arrays.asList("overworld", "nether", "end");
        getConfig().set("general.regeneration.worlds.definedWorlds", definedWorlds);

        // Are the defined worlds blacklisted or whitelisted?
        getConfig().set("general.regeneration.worlds.isBlacklist", true);


        // Should factions be checked for regeneration?
        getConfig().set("integration.regeneration.factions.enabled", true);

        // List of factions to regenerate land for.
        List<String> factions = Arrays.asList("WILDERNESS", "WARZONE");
        getConfig().set("integration.regeneration.factions.definedFactions", factions);

        // Saves the config file.
        saveConfig();
    }
    
    public void disablePlugin() {
     Bukkit.getServer().getPluginManager().disablePlugin(this);
    }
        
    public void throwMessage(String type, String message) {
        if (type == "info") {
            log.info(message);
        } else {
            if (type == "warning") {
                log.warning(message); 
            } else {
                if (type == "severe") {
                    log.severe(message);
                    this.disablePlugin();
                } else {
                    this.throwMessage("severe","Fatal call to throwMessage, valid message types are severe,info,warning");
                }
            }
        }
    }
}
