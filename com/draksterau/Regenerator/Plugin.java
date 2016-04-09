package com.draksterau.Regenerator;

import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import com.draksterau.Regenerator.tasks.RegenTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
            checkDependencies();
            if (this.isEnabled()) {
                if (config.getBoolean("general.regeneration.enabled") == true) {
                    log.log(Level.INFO, "Starting Regenerator v{0}...", getConfig().getString("version"));
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new RegenTask(this), 15 * 20);
                } else {
                    this.disablePlugin();
                }
            }
    }
    
    
    public void checkDependencies() {
        boolean shouldDisable = false;
        if (Bukkit.getServer().getPluginManager().getPlugin("Factions").isEnabled() != getConfig().getBoolean("integration.regeneration.factions.enabled")) {
            if (Bukkit.getServer().getPluginManager().getPlugin("Factions").isEnabled()) {
                log.severe("Factions is enabled, however integration is disabled. For chunks to be properly protected you MUST configure integration.");
                shouldDisable = true;
            } else {
                log.severe("Factions integration is enabled however factions is not installed. Please turn it off before loading the plugin.");
                shouldDisable = true;
            }
        }
        for (String worldName : getConfig().getStringList("general.regeneration.worlds.definedWorlds")) {
            if (!(Bukkit.getServer().getWorld(worldName) instanceof World)) {
                log.log(Level.SEVERE, "Defined world: {0} does not exist. Remove this from the config before proceeding.", worldName);
                shouldDisable = true;
            }
        }
        if (Bukkit.getServer().getPluginManager().getPlugin("Factions").isEnabled()) {
            for (String Faction : getConfig().getStringList("integration.regeneration.factions.definedFactions")) {
                if (!factionsIntegration.factionExists(Faction)) {
                    log.log(Level.SEVERE, "Defined faction: {0} does not exist. Remove this from the config before proceeding.", Faction);
                    shouldDisable = true;
                }
            }
        }
        
        if (shouldDisable == true) {
            log.log(Level.SEVERE, "Dependency checks failed.... cannot continue...");
            this.disablePlugin();
        } else {
            log.log(Level.INFO, "Dependency checks passed.... continuing...");
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
        int worldInterval = 15;
        getConfig().set("general.regeneration.worlds.interval", worldInterval);
        
        // Offset in seconds between world tasks
        int offset = 15;
        getConfig().set("general.regeneration.worlds.offset", offset);
        
        // Seconds between Regeneration sweeeps
        int chunkInterval = 15;
        getConfig().set("general.regeneration.worlds.chunks.interval", chunkInterval);
        
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
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("Reload")) {
                if (sender.hasPermission("regenerator.reload") || sender.isOp()) {
                    Bukkit.getServer().getScheduler().cancelTasks(this);
                    this.onEnable();
                    sender.sendMessage("Regenerator has been reloaded. All World Regeneration tasks have been reset.");
                    return true;
                }
            }
        }
        return false;
    }
}
