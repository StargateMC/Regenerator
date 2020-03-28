package com.draksterau.Regenerator;

//import com.draksterau.Regenerator.commands.RegeneratorCommand;
import com.draksterau.Regenerator.Handlers.MsgType;
import com.draksterau.Regenerator.listeners.eventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import org.bukkit.event.Listener;
import com.draksterau.Regenerator.integration.Integration;
import com.draksterau.Regenerator.tasks.lagTask;
import com.draksterau.Regenerator.tasks.regenTask;
import com.draksterau.Regenerator.Handlers.RConfig;
import com.draksterau.Regenerator.Handlers.RLang;
import com.draksterau.Regenerator.Handlers.RUtils;
import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.commands.RegeneratorCommand;
import java.io.File;
import org.bukkit.entity.Player;

public class RegeneratorPlugin extends JavaPlugin implements Listener {
    
    // Load the RUtils module on enable.
    public RUtils utils;
        
    // Config gets loaded here in onEnable()
    public RConfig config;
    
    public RLang lang;

    public List<List<String>> availableIntergrations = new ArrayList<List<String>>();
    
    public List<Integration> loadedIntegrations = new ArrayList<Integration>();
    
    public List<RWorld> loadedWorlds = new ArrayList<RWorld>();
    
    
    public int chunksToRegenCached = 0;
    
    public Player fakePlayer = null;
    
    public eventListener listener = null;
    
    public boolean isParseActive = false;
    
    public RLang getOrInitLang(String language) {
        if (this.lang == null) this.lang = new RLang(this, language);
        return this.lang;
    }
    public RConfig getOrInitConfig() {
        if (this.config == null) this.config = new RConfig(this);
        return this.config;
    }
    
    @Override
    public void onEnable () {
        // Load the RUtils module.
        utils = new RUtils(this);
        // Config gets loaded here in onEnable()
        getOrInitConfig();
        if (!this.isEnabled()) return; // If Config or lang loading fails, stop enabling the plugin.
        utils.throwMessage(MsgType.INFO, String.format(lang.getForKey("messages.pluginLoading"), config.configVersion));
        utils.initAvailableIntegrations();
        utils.loadIntegrations();
        if (this.isEnabled()) {
            utils.throwMessage(MsgType.INFO, "Detected server version: " + Bukkit.getVersion() + ", Bukkit API Version: " + Bukkit.getBukkitVersion());
            if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                if (Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion().startsWith("7")) {
                    utils.throwMessage(MsgType.INFO, String.format(lang.getForKey("messages.WorldEditLoaded"), Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion()));
                } else {
                    if (Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion().startsWith("1.15")) {
                        utils.throwMessage(MsgType.INFO, String.format(lang.getForKey("messages.AsyncWorldEditLoaded"), Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion()));
                    } else {
                        utils.throwMessage(MsgType.SEVERE, String.format(lang.getForKey("messages.WorldEditIncompatible"), Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion(), "7.x"));
                        this.disablePlugin();
                    }
                }
            } else {
                    utils.throwMessage(MsgType.SEVERE, String.format(lang.getForKey("messages.WorldEditMissing"), "v7.x"));
                    this.disablePlugin();
            }
            utils.throwMessage(MsgType.INFO, String.format(lang.getForKey("messages.pluginStarting"), config.configVersion));
            if (loadedIntegrations.isEmpty()) {
                if (config.noGriefRun) {
                    utils.throwMessage(MsgType.WARNING, lang.getForKey("messages.warningNoIntegrationsNoGriefRunEnabled"));
                } else {
                    utils.throwMessage(MsgType.WARNING, lang.getForKey("messages.warningNoIntegrationsNoGriefRunDisabled"));
                    utils.throwMessage(MsgType.INFO, lang.getForKey("messages.integrationSupportMessage"));
                    utils.iterateIntegrations();
                    utils.throwMessage(MsgType.SEVERE, lang.getForKey("messages.noGriefRunPrompt"));
                }
            }
            
            if (!config.enableUnknownProtectionDetection) utils.throwMessage(MsgType.INFO, lang.getForKey("messages.unknownProtectionDetectionInactive"));
            if (config.enableUnknownProtectionDetection) {
                if (!Bukkit.getBukkitVersion().contains("1.15.2")) {
                    utils.throwMessage(MsgType.WARNING, String.format(lang.getForKey("messages.unknownProtectionDetectionUnsupported"), "1.15.2", Bukkit.getVersion(),Bukkit.getBukkitVersion()));
                    config.enableUnknownProtectionDetection = false;
                } else {
                    utils.throwMessage(MsgType.INFO, lang.getForKey("messages.unknownProtectionDetectionActive"));
                    if (utils.uuidInUse(config.fakePlayerUUID)) {
                        utils.throwMessage(MsgType.WARNING, String.format(lang.getForKey("messages.fakePlayerUUIDInUse"), config.fakePlayerUUID));
                    } else {
                        utils.throwMessage(MsgType.INFO, String.format(lang.getForKey("messages.fakePlayerUUIDNotInUse"), config.fakePlayerUUID));
                    }
                }
            }
            if (config.debugMode) utils.throwMessage(MsgType.INFO, lang.getForKey("messages.debugModeEnabled"));
            if (!config.debugMode) utils.throwMessage(MsgType.INFO, lang.getForKey("messages.debugModeDisabled"));
            
            if (this.isEnabled()) {
                utils.loadWorlds();
                
                // This registers all event listeners.
                try {
                    if (this.listener == null) {
                        this.listener = new eventListener(this);
                        getServer().getPluginManager().registerEvents(this.listener, this);
                        utils.throwMessage(MsgType.INFO, "Successfully registered Event Listeners!");
                    } else {
                        utils.throwMessage(MsgType.INFO, "Skipping registering of event listeners, not required for a reload!");
                    }
                } catch (Exception e) {
                    utils.throwMessage(MsgType.SEVERE, "Failed to start event listeners. Please report this (and the below error) to the developer!");
                    if (config.debugMode) e.printStackTrace();
                    this.disablePlugin();
                }
                // This registers a repeating task to measure 1 tick, so we can accurately  get TPS.
                try {
                    new lagTask().runTaskTimer(this, 100L, 1L);
                    utils.throwMessage(MsgType.INFO, "Successfully registered TPS Monitor!");
                } catch (Exception e) {
                    utils.throwMessage(MsgType.SEVERE, "Failed to start TPS monitor. Please report this (and the below error) to the developer!");
                    if (config.debugMode) e.printStackTrace();
                    this.disablePlugin();
                }
                // This registers the regeneration task.
                try {
                    new regenTask(this).runTaskTimerAsynchronously(this,100, config.parseInterval * 20);
                    utils.throwMessage(MsgType.INFO, "Successfully registered Regeneration Task!");
                } catch (Exception e) {
                    utils.throwMessage(MsgType.SEVERE, "Failed to start regeneration task. Please report this (and the below error) to the developer!");
                    if (config.debugMode) e.printStackTrace();
                    this.disablePlugin();
                }
                utils.throwMessage(MsgType.INFO, String.format(this.lang.getForKey("messages.parseSchedule"), "5", String.valueOf(config.parseInterval)));
                utils.throwMessage(MsgType.INFO, String.format(this.lang.getForKey("messages.parseChunksPerMax"), config.numChunksPerParse, Math.floor((config.parseInterval / config.numChunksPerParse))));

            }
        }
    }
    
    public void disablePlugin() {
     Bukkit.getServer().getPluginManager().disablePlugin(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        RegeneratorCommand RegeneratorCommand = new RegeneratorCommand(this, sender, cmd, label, args);
        return RegeneratorCommand.doCommand();
    }
    

    
    public boolean isBacklogged() {
        return (this.chunksToRegenCached > config.numChunksPerParse);
    }
    public double parseQueue() {
        try {
            return (this.chunksToRegenCached / config.numChunksPerParse);
        } catch (Exception e) {
            return 0;
        }
    }
    public long getQueueDelay() {
        try {
            return (long) ((this.chunksToRegenCached / config.numChunksPerParse) * config.parseInterval);
        } catch (Exception e) {
            return 1;
        }
    }

}
