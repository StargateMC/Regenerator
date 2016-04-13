package com.draksterau.Regenerator;

import com.draksterau.Regenerator.commands.RegeneratorCommand;
import com.draksterau.Regenerator.config.chunkConfigHandler;
import com.draksterau.Regenerator.config.worldConfigHandler;
import com.draksterau.Regenerator.listeners.eventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import com.draksterau.Regenerator.integration.Integration;
import com.draksterau.Regenerator.listeners.integrationListener;
import com.draksterau.Regenerator.tasks.lagTask;

public class RegeneratorPlugin extends JavaPlugin implements Listener {
    
    // Config gets loaded here in onEnable()
    static FileConfiguration config;
        
    private Logger log = Logger.getLogger("Minecraft");

    public List<String> availableIntergrations = new ArrayList<String>();
    
    List<Integration> loadedIntegrations = new ArrayList<Integration>();
    
    public String definedWorlds;
   
    public boolean isPaused = false;
    
    @Override
    public void onEnable () {
            throwMessage("info", "Loaded Regenerator!");
            getConfiguration();
            initAvailableIntegrations();
            loadIntegrations();
            initialiseWorlds();
            if (this.isEnabled()) {
                    throwMessage("info", "Starting Regenerator v" + getConfig().getString("config-version"));
                    if (!loadedIntegrations.isEmpty()) {
                        getServer().getPluginManager().registerEvents(new integrationListener(this), this);
                    } else {
                        if (getConfig().getBoolean("no-grief-run")) {
                            throwMessage("warning", "No supported grief protection plugins found. No land will be protected from regeneration via external plugins!");
                        } else {
                            throwMessage("severe", "No supported grief protection plugins found. You must set 'no-grief-run' to true in config before Regenerator will load. This is accepting you need to configure things properly OR YOU WILL LOSE CHUNKS!");
                        }
                    }
                    if (this.isEnabled()) {
                        getServer().getPluginManager().registerEvents(new eventListener(this), this);
                        getServer().getScheduler().scheduleSyncRepeatingTask(this, new lagTask(), 100L, 1L);
                    }
            }
    }
    
    public void initAvailableIntegrations() {
        availableIntergrations.add("Factions");
        availableIntergrations.add("GriefPrevention");
        availableIntergrations.add("WorldGuard");
        availableIntergrations.add("RedProtect");
        availableIntergrations.add("FactionsUUID");
        for (String name : availableIntergrations) {
            throwMessage("info", name + " integration module initialised.");
        }
    }
    
    public boolean isLagOK() {
     if (lagTask.getTps() > getConfig().getDouble("min-tps-regen")) {
         return true;
     } else {
         return false;
     }
    }
    public void initialiseWorlds() {
        for (World world : Bukkit.getWorlds()) {
            throwMessage("info", "Loading World: " + world.getName());
            worldConfigHandler wConfig = new worldConfigHandler(this, world);
            wConfig.configureWorld();
        }
    }
    
    public Integration getLoadedIntegration(String name) {
        for (Integration integration : loadedIntegrations) {
            if (integration.getPluginName().equals(name)) {
                return integration;
            }
        }
        return null;
    }
    
    public void loadIntegrationFor(String plugin) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                Class<?> integrationClass = Class.forName("com.draksterau.Regenerator.integration." + plugin + "Integration");
                if (Integration.class.isAssignableFrom(integrationClass)) {
                    Integration integration = (Integration) integrationClass.newInstance();
                    integration.plugin = plugin;
                    integration.RegeneratorPlugin = this;
                    integration.validateConfig();
                    loadedIntegrations.add(integration);
                    throwMessage("info", "Detected Plugin: " + integration.getPluginName() + " v" + integration.getPluginVersion() + ": Loading integration!");
                }
            } else {
                throwMessage("warning", "Didn't detect Plugin: " + plugin + ". Disabling " + plugin + " integration module.");
            }
        } catch (ClassNotFoundException ex) {
            throwMessage("severe", "Failed to load integration for plugin: " + plugin + ". Please contact Bysokar for support!");
        } catch (InstantiationException | IllegalAccessException ex) {
            throwMessage("severe", "Failed to load integration for plugin: " + plugin + " (Exception: " + ex.getMessage() + " is not compatible!)");
            ex.printStackTrace();
        }
        
    }
    
    public boolean isEnabledIntegration(String plugin) {
        for (Integration integration : loadedIntegrations) {
            if (integration.getPluginName().equals(plugin)) {
                return true;
            }
        }
        return false;
    }
    
    public void disableIntegrationFor(String plugin) {
        Integration toDisable = null;
        for (Integration integration : loadedIntegrations) {
            if (integration.getPluginName().equals(plugin)) {
                toDisable = integration;
            }
        }
        if (toDisable != null) {
            loadedIntegrations.remove(toDisable);
        }
    }
    public void loadIntegrations() {
        for (String plugin : availableIntergrations) {
            loadIntegrationFor(plugin);
        }
    }
    
    public void getConfiguration() {
        saveDefaultConfig();   
        if (getConfig().get("config-version").equals(this.getDescription().getVersion())) {
            // Reloads configuration.
            throwMessage("info", "Loading Configuration...");
            this.reloadConfig();          
        } else {
            throwMessage("warning","Version mismatch between plugin and config file, version is: " + this.getDescription().getVersion() + " whereas config is for version: " + getConfig().getString("config-version"));
            throwMessage("warning","Configuration load failed, validating & updating config file...");
            configure();
            saveConfig();
        }
        config = getConfig();
    }
    
    public void tellPlayersOnWorld(World world, String message) {
        List<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isOnline() && !player.isOp() && !player.hasPermission("regenerator.notify")) {
                    player.sendMessage(getFancyName() + " " + message);
                }                
            }
        }
        tellAllNotified(message);
    }
        
    public void tellAllNotified(String message) {
        for (World world : Bukkit.getWorlds()) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    if (player.isOp() || player.hasPermission("regenerator.notify")) {
                        player.sendMessage(getFancyName() + ChatColor.BLUE + "Notify" + ChatColor.GRAY + ":" + message);
                    }                
                }
            }
        }
    }
    public void configure () {
        
        // Regenerator Version
        if (!getConfig().isSet("config-version") || !getConfig().getString("config-version").equals(this.getDescription().getVersion())) {
            getConfig().set("config-version", this.getDescription().getVersion());
        // TODO: Set defaults, validate past.
        // Saves the config file.
        if (!getConfig().isSet("no-grief-run")) {
            getConfig().set("no-grief-run", false);
        }
        if (!getConfig().isSet("min-tps-regen")) {
            getConfig().set("min-tps-regen", 10.0);
        }
        if (!getConfig().isSet("regen-on-player-change-chunk")) {
            getConfig().set("regen-on-player-change-chunk", true);
        }
        if (!getConfig().isSet("regen-on-chunk-load")) {
            getConfig().set("regen-on-chunk-load", true);
        }
        if (!getConfig().isSet("regen-on-chunk-unload")) {
            getConfig().set("regen-on-chunk-unload", true);
        }
        if (!getConfig().isSet("regen-on-world-load")) {
            getConfig().set("regen-on-world-load", false);
        }
        if (!getConfig().isSet("regen-on-world-unload")) {
            getConfig().set("regen-on-world-unload", false);
        }
        saveConfig();
        }
    }
    
    public void disablePlugin() {
     Bukkit.getServer().getPluginManager().disablePlugin(this);
    }
    
    public  void throwMessage(String type, String message) {
        if ("info".equals(type)) {
            log.log(Level.INFO, "[{0}] {1}", new Object[]{this.getDescription().getName(), message});
        } else {
            if ("warning".equals(type)) {
                log.log(Level.WARNING, "[{0}] {1}", new Object[]{this.getDescription().getName(), message});
            } else {
                if ("severe".equals(type)) {
                    log.log(Level.SEVERE, "[{0}] {1}", new Object[]{this.getDescription().getName(), message});
                    this.disablePlugin();
                } else {
                    this.throwMessage("severe","Fatal call to throwMessage, valid message types are severe,info,warning");
                }
            }
        }
    }
    
    public Chunk getSenderChunk(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            return player.getLocation().getChunk(); 
        } else {
            return null;
        }
    }
    public Player getSenderPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            return player;
        } else {
            return null;
        }
    }
    
    public String getFancyName() {
        return ChatColor.RED + "[" + ChatColor.DARK_GREEN + this.getDescription().getName() + ChatColor.RED + "] " + ChatColor.GRAY;
    }
    
    public void moveOfflinePlayers(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        List<Player> players = new ArrayList<Player>();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!player.isOnline()) {
                    player.teleport(chunk.getWorld().getSpawnLocation());
                    players.add(player);
                }
            }
        }
        tellPlayersOnWorld(chunk.getWorld(), "The following players have been moved to the world spawn for world: " + chunk.getWorld().getName() + ": " + players.toString());
    }

    public int onlinePlayersInChunk(Chunk chunk) {
        int count = 0;
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isOnline()) {
                    count++;
                }
            }
        }
        return count;
    }
    public double distance(double sx, double sy, double sz, double dx, double dy, double dz) {
        double distance = Math.sqrt(Math.pow(sx-dx,2) + Math.pow(sx-dx,2) + Math.pow(sz-dz,2));
    //    System.out.println("Distance calculated as : " + distance);
        return distance;
    }
    public boolean autoRegenRequirementsMet(Chunk chunk) {

        worldConfigHandler wConfig = new worldConfigHandler(this, chunk.getWorld());
        chunkConfigHandler cConfig = new chunkConfigHandler(this, chunk);

        // Blocked at the world level.
        if (!wConfig.getAutoRegenEnabled()) {
            return false;
        }
        
        if (!cConfig.getAutoRegen()) {
            // Blocked as the chunk is disabled for autoregen manually.
            return false;
        }
        // Blocked as the skip radius includes this chunk.
        
        if (wConfig.getSkipRadius() > distance(chunk.getX(), 100.0, chunk.getZ(), chunk.getWorld().getSpawnLocation().getBlockX(), 100.0, chunk.getWorld().getSpawnLocation().getBlockZ())) {
            // Blocked due to skip radius.
            return false;
        }
        
        // Blocked at the integration level.
        for (Integration integration : loadedIntegrations) {
            if (!integration.shouldChunkAutoRegen(chunk)) {
                return false;
            }
        }
        
        // Not blocked.
        return true;
    }
    
    public boolean canManuallyRegen(Player player, Chunk chunk) {
        
        worldConfigHandler wConfig = new worldConfigHandler(this, chunk.getWorld());
        chunkConfigHandler cConfig = new chunkConfigHandler(this, chunk);
        
        if (!cConfig.getManualRegen() && wConfig.getManualRegen()) {
            // Blocked as the chunk is disabled for autoregen manually.
            return false;
        }
        if (!wConfig.getManualRegen()) {
            return false;
        }
        
        // This returns true if the player has the override permission node but only for claimed land.
        if (player.hasPermission("regenerator.regen.override") && getIntegrationForChunk(chunk) != null) {
            return true;
        }

        
        // Is it unclaimed?
        if (getIntegrationForChunk(chunk) == null) {
            if (player.hasPermission("regenerator.regen.unclaimed")) {
                return true;
            }
        }
        
        // Blocked at the integration level.
        for (Integration integration : loadedIntegrations) {
            if (!integration.canPlayerRegen(player,chunk)) {
                return false;
            }
        }
        
        return true;
    }
    
    public Integration getIntegrationForChunk(Chunk chunk) {
        for (Integration integration : loadedIntegrations) {
            if (integration.isChunkClaimed(chunk)) {
                return integration;
            }
        }
        return null;
    }
    public int getCountIntegration(Chunk chunk) {
        int count = 0;
        for (Integration integration : loadedIntegrations) {
            if (integration.isChunkClaimed(chunk)) {
                count++;
            }
        }
        return count;
    }
    
    
    public long convertMsToSecond(long newMS, long oldMS) {
        return ((newMS - oldMS) / 1000);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        RegeneratorCommand RegeneratorCommand = new RegeneratorCommand(this, sender, cmd, label, args);
        return RegeneratorCommand.doCommand();
    }
    
    
    public boolean validateChunkInactivity (Chunk chunk, boolean isLoading) {
        long secSinceLastRegen = 0;
        long secSinceLastPlaced = 0;
        long secSinceLastBroken = 0;
        
        chunkConfigHandler cConfig = new chunkConfigHandler(this, chunk);
        
        if (cConfig.getLastPlaced() != 0) {
            secSinceLastPlaced = (System.currentTimeMillis() - cConfig.getLastPlaced()) / 1000;
        }
        if (cConfig.getLastBroken() != 0) {
            secSinceLastBroken = (System.currentTimeMillis() - cConfig.getLastBroken()) / 1000;
        }
        if (cConfig.getLastRegen() != 0) {
            secSinceLastRegen = (System.currentTimeMillis() - cConfig.getLastRegen()) / 1000;
        }
        // The below statement says a chunk that has never been active should never be regenerated.
        if (secSinceLastRegen == 0 && secSinceLastBroken == 0 && secSinceLastPlaced == 0) {
            return false;
        } else {
            // This loads the chunk interval for the world.
            worldConfigHandler wConfig = new worldConfigHandler(this, chunk.getWorld());
            int chunkInterval = wConfig.getChunkInterval();
            // This statement checks to see if the last time the chunk had a block broken or placed is greater than the chunkInterval for the world.
            if (secSinceLastPlaced >= chunkInterval || secSinceLastBroken >= chunkInterval) {
                return true;
            } else {
                return false;
            }
        }
    }
    

}
