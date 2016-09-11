package com.draksterau.Regenerator;

//import com.draksterau.Regenerator.commands.RegeneratorCommand;
import com.draksterau.Regenerator.listeners.eventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import org.bukkit.event.Listener;
import com.draksterau.Regenerator.integration.Integration;
import com.draksterau.Regenerator.tasks.lagTask;
import com.draksterau.Regenerator.tasks.regenTask;
import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RConfig;
import com.draksterau.Regenerator.Handlers.RLang;
import com.draksterau.Regenerator.Handlers.RUtils;
import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.commands.RegeneratorCommand;

public class RegeneratorPlugin extends JavaPlugin implements Listener {
    
    // Load the RUtils module on enable.
    public RUtils utils;
        
    // Config gets loaded here in onEnable()
    public RConfig config;
    
    public RLang lang;

    public List<List<String>> availableIntergrations = new ArrayList<List<String>>();
    
    public List<Integration> loadedIntegrations = new ArrayList<Integration>();
    
    public List<RWorld> loadedWorlds = new ArrayList<RWorld>();
    
    
    @Override
    public void onEnable () {
        // Loads the language file.
        lang = new RLang(this);
        // Load the RUtils module.
        utils = new RUtils(this);
        // Config gets loaded here in onEnable()
        config = new RConfig(this);
        
        utils.throwMessage("info", String.format(lang.getForKey("messages.pluginLoading"), config.configVersion));
        utils.initAvailableIntegrations();
        utils.loadIntegrations();
        if (this.isEnabled()) {
            utils.throwMessage("info", String.format(lang.getForKey("messages.pluginStarting"), config.configVersion));
            if (loadedIntegrations.isEmpty()) {
                if (config.noGriefRun) {
                    utils.throwMessage("warning", "No supported grief protection plugins found. No land will be protected from regeneration via external plugins!");
                } else {
                    utils.throwMessage("warning", "No supported grief protection plugins found. You must acknowledge that you must configure the plugin properly or risk losing chunks.");
                    utils.throwMessage("info", "Regenerator supports the following plugins:");
                    utils.iterateIntegrations();
                    utils.throwMessage("severe", "You must set 'noGriefRun' to true in config before Regenerator will load without integrations.");
                }
            }
            if (this.isEnabled()) {
                utils.loadWorlds();
                
                // This registers all event listeners.
                getServer().getPluginManager().registerEvents(new eventListener(this), this);
                // This registers a repeating task to measure 1 tick, so we can accurately  get TPS.
                getServer().getScheduler().runTaskTimer(this, new lagTask(), 100L, 1L);
                // This registers the regeneration task.
                getServer().getScheduler().runTaskTimerAsynchronously(this, new regenTask(this), 1200, config.parseInterval * 20);
                utils.throwMessage("info", String.format(this.lang.getForKey("messages.parseSchedule"), "30", String.valueOf(config.parseInterval)));
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

}
