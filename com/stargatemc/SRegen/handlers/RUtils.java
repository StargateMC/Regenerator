/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stargatemc.SRegen.handlers;

import com.stargatemc.SRegen.SRegen;
import com.stargatemc.SRegen.integration.Integration;
import com.stargatemc.SRegen.tasks.lagTask;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author draks
 */
public class RUtils extends RObject {

    public RUtils(SRegen plugin) {
        super(plugin);
    }
    
    // Moves offline players on a chunk to the spawn of the world.
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
    // Gets an RWorld
    public RWorld getRWorldForWorld(World world) {
        for (RWorld RWorld : plugin.loadedWorlds) {
            if (RWorld.world.equals(world)) {
                RWorld.loadData();
                return RWorld;
            }
        }
        return null;
    }
    
    // Returns a formatted string of Enabled or disabled
    
    public String getStatusForBoolean(boolean bool) {
        if (bool) return ChatColor.GREEN + "Enabled" + ChatColor.GRAY;
        return ChatColor.RED + "Disabled" + ChatColor.GRAY;
    }
    
    
    // This verifies a chunk is inactive and unclaimed.
    public boolean autoRegenRequirementsMet(Chunk chunk) {
        
        RWorld RWorld = getRWorldForWorld(chunk.getWorld());
        
        // If the world is not loaded, do nothing.
        if (RWorld == null) return false;
        // Blocked at the world level.
        if (!RWorld.canAutoRegen()) {
            return false;
        }
                
        // This handles the world configuration for borders and the skip radius.
        if (RWorld.minBlockAutoRegen >= distance(chunk.getX(), 100.0, chunk.getZ(), chunk.getWorld().getSpawnLocation().getBlockX(), 100.0, chunk.getWorld().getSpawnLocation().getBlockZ())) return false;
        if (RWorld.maxBlockAutoRegen != 0 && RWorld.maxBlockAutoRegen > distance(chunk.getX(), 100.0, chunk.getZ(), chunk.getWorld().getSpawnLocation().getBlockX(), 100.0, chunk.getWorld().getSpawnLocation().getBlockZ())) return false;
        // Blocked at the integration level.
        for (Integration integration : plugin.loadedIntegrations) {
            if (!integration.shouldChunkAutoRegen(chunk)) {
                return false;
            }
        }

        // Not blocked.
        return true;
    }
    // This simply lists supported plugins and versions.
    public void iterateIntegrations() {
        for (List<String> integration : plugin.availableIntergrations) {
            String name = integration.get(2).replace("Integration", "");
            throwMessage("info", ChatColor.LIGHT_PURPLE + name);
        }
    }
    // Formats a message and categorises it instead of using logger directly.
    public  void throwMessage(String type, String message) {

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if ("info".equals(type)) {
            console.sendMessage(getFancyName() + ChatColor.DARK_AQUA + "[" + type.toUpperCase() + "]: " + message);
        } else {
            if ("warning".equals(type)) {
                console.sendMessage(getFancyName() + ChatColor.YELLOW + "[" + type.toUpperCase() + "]: " + message);
            } else {
                if ("severe".equals(type)) {
                    console.sendMessage(getFancyName() + ChatColor.RED + "[" + type.toUpperCase() + "]: " + message);
                    plugin.disablePlugin();
                } else {
                    if ("new".equals(type)) {
                        console.sendMessage(getFancyName() + ChatColor.LIGHT_PURPLE + "[" + type.toUpperCase() + "]: " + message);
                    } else {
                        this.throwMessage("severe",String.format(this.plugin.lang.getForKey("messages.errorThrowingMessage")));
                    }
                }
            }
        }
    }
    
    // Tells all players on a world, the specified message.
    public void tellPlayersOnWorld(World world, String message) {
        List<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isOnline() && !player.isOp() && !player.hasPermission("SRegen.notify")) {
                    player.sendMessage(getFancyName() + " " + message);
                }                
            }
        }
    }
    
    // Returns a formatted version of the plugins name.
    public String getFancyName() {
        return ChatColor.RED + "[" + ChatColor.DARK_GREEN + plugin.getDescription().getName() + ChatColor.RED + "] " + ChatColor.GRAY;
    }
    
    // Gets the players nearby to a chunk based on a distance value.
    public List<Player> getPlayersNearChunk(RChunk rChunk, int distance) {
        List<Player> playersNearby = new ArrayList<Player>();
        List<Player> playersOnWorld = rChunk.getChunk().getWorld().getPlayers();
        for (Player p : playersOnWorld) {
            // Skip the player if they are further away.
            if (distance(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ(), rChunk.getChunk().getX(), p.getLocation().getBlockY(), rChunk.getChunk().getZ()) > distance) continue; 
            playersNearby.add(p);
        }
        return playersNearby;
    }
    
    
    // This will be used for ingame debugging.
    public void sendNotifyMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("SRegen.notify")) continue;
            p.sendMessage(getFancyName() + " " + ChatColor.GRAY + message);
        }
    }
    
    // Gets the distance between to X,Y,Z coordinates.
    public double distance(double sx, double sy, double sz, double dx, double dy, double dz) {
        double distance = Math.sqrt(Math.pow(sx-dx,2) + Math.pow(sx-dx,2) + Math.pow(sz-dz,2));
        return distance;
    }
    public Location isWarpCoreNearby(Chunk c) {
        World world = c.getWorld();
        for (Chunk ch : world.getLoadedChunks()) {
            for (BlockState te : ch.getTileEntities()) {
                if (te.getType().name().equals("WARPDRIVE_BLOCKSHIPCORE")) {
                    if (distance(te.getLocation().getX(), 100.0, te.getLocation().getZ(),(c.getX()*16), 100.0,(c.getZ()*16)) < 182) {
                        return te.getLocation();
                    }
                }
            }
        }
        return null;
    }
    // Gets the count of online players in a chunk.
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

    // Checks a chunk for an integration (claim) on the chunk
    public Integration getIntegrationForChunk(Chunk chunk) {
        for (Integration integration : plugin.loadedIntegrations) {
            if (integration.isChunkClaimed(chunk)) {
                return integration;
            }
        }
        return null;
    }
    // Checks for the number of integrations (claim plugins) that are claiming a chunk
    public int getCountIntegration(Chunk chunk) {
        int count = 0;
        for (Integration integration : plugin.loadedIntegrations) {
            if (integration.isChunkClaimed(chunk)) {
                count++;
            }
        }
        return count;
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
    
    public boolean canManuallyRegen(Player player, Chunk chunk) {
        
        RWorld RWorld = getRWorldForWorld(chunk.getWorld());
        
        // If the world is not loaded, do nothing.
        if (RWorld == null) return false;
        
        // If the world has manual regen disabled, do not allow it.
        if (!RWorld.canManualRegen()) {
            return false;
        }
        
        // This returns true if the player has the override permission node but only for claimed land.
        if (player.hasPermission("SRegen.regen.override") && getIntegrationForChunk(chunk) != null) {
            return true;
        }

        
        // Is it unclaimed?
        if (getIntegrationForChunk(chunk) == null) {
            if (player.hasPermission("SRegen.regen.unclaimed")) {
                return true;
            }
        }
        
        // Blocked at the integration level.
        for (Integration integration : plugin.loadedIntegrations) {
            if (!integration.canPlayerRegen(player,chunk)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void loadWorlds() {
        for (World world : Bukkit.getWorlds()) {
            throwMessage("info", String.format(this.plugin.lang.getForKey("messages.loadingWorld"), world.getName()));
            RWorld RWorld = new RWorld(plugin, world);
            plugin.loadedWorlds.add(RWorld);
            throwMessage("info", String.format(this.plugin.lang.getForKey("messages.loadedWorld"), world.getName()));
        }
    }
    public void initAvailableIntegrations() {
        List<String> Towny = new ArrayList<String>();
        Towny.add("Towny");
        Towny.add("0.91");
        Towny.add("TownyIntegration");
        plugin.availableIntergrations.add(Towny);
        List<String> FactionsOne = new ArrayList<String>();
        FactionsOne.add("Factions");
        FactionsOne.add("1.8");
        FactionsOne.add("FactionsOneIntegration");
        plugin.availableIntergrations.add(FactionsOne);
        List<String> FactionsUUID = new ArrayList<String>();
        FactionsUUID.add("Factions");
        FactionsUUID.add("1.6");
        FactionsUUID.add("FactionsUUIDIntegration");
        plugin.availableIntergrations.add(FactionsUUID);
        List<String> GriefPrevention = new ArrayList<String>();
        GriefPrevention.add("GriefPrevention");
        GriefPrevention.add("14");
        GriefPrevention.add("GriefPreventionIntegration");
        plugin.availableIntergrations.add(GriefPrevention);
        List<String> WorldGuard = new ArrayList<String>();
        WorldGuard.add("WorldGuard");
        WorldGuard.add("6");
        WorldGuard.add("WorldGuardIntegration");
        plugin.availableIntergrations.add(WorldGuard);
        List<String> RedProtect = new ArrayList<String>();
        RedProtect.add("RedProtect");
        RedProtect.add("6.5");
        RedProtect.add("RedProtectIntegration");
        plugin.availableIntergrations.add(RedProtect);
        List<String> Factions = new ArrayList<String>();
        Factions.add("Factions");
        Factions.add("2.8");
        Factions.add("FactionsIntegration");
        plugin.availableIntergrations.add(Factions);
        List<String> Landlord = new ArrayList<String>();
        Landlord.add("Landlord");
        Landlord.add("1.3");
        Landlord.add("LandlordIntegration");
        plugin.availableIntergrations.add(Landlord);
    }
    
    public boolean isLagOK() {
     if (lagTask.getTps() >= plugin.config.minTpsRegen) {
         return true;
     } else {
         return false;
     }
    }
    
    public void clearEntitiesFromChunk (RChunk rChunk) {
        int count = 0;
        for (Entity e : rChunk.getChunk().getEntities()) {
            if (plugin.config.excludeEntityTypesFromRegeneration.contains(e.getType().getName())) continue;
            if (e instanceof Player) continue;
            e.remove();
            count++;
        }   
        throwMessage("info", "Removed " + count + " entities from chunk!");
    }
    
    public boolean validateChunkInactivity (RChunk rChunk) {
        
        long secSinceLastActive = 0;
        
        RWorld RWorld = getRWorldForWorld(rChunk.getWorld());       
        
        // IF the chunk doesnt exist, do nothing.
        if (rChunk == null) return false;
        
        if (rChunk.lastActivity != 0) {
            secSinceLastActive = (System.currentTimeMillis() - rChunk.lastActivity) / 1000;
        }
        
        // If the chunk has never been modified, dont do anything.
        if (secSinceLastActive == 0) return false;

        if (secSinceLastActive < RWorld.getIntervalSecs()) return false;

        return true;
    }
    
    public Integration getLoadedIntegration(String name) {
        for (Integration integration : plugin.loadedIntegrations) {
            if (integration.getPluginName().equals(name)) {
                return integration;
            }
        }
        return null;
    }
    
    public void loadIntegrationFor(List<String> plugins) {
        String[] module = plugins.toArray(new String[plugins.size()]);
        try {
            if (Bukkit.getPluginManager().isPluginEnabled(module[0])) {
                if (Bukkit.getPluginManager().getPlugin(module[0]).getDescription().getVersion().startsWith(module[1])) {
                    Class<?> integrationClass = Class.forName("com.draksterau.SRegen.integration." + module[2]);
                    if (Integration.class.isAssignableFrom(integrationClass)) {
                        Integration integration = (Integration) integrationClass.newInstance();
                        integration.plugin = module[0];
                        integration.SRegenPlugin = plugin;
                        integration.validateConfig();
                        plugin.loadedIntegrations.add(integration);

                        throwMessage("info", "[" + module[2] + "] " + String.format(plugin.lang.getForKey("messages.detectedAndLoadingIntegration"), integration.getPluginName(), integration.getPluginVersion(), module[2]));
                    }
                } else {
                    throwMessage("warning", "[" + module[2] + "] " + String.format(plugin.lang.getForKey("messages.detectedInvalidVersionForIntegration"), module[0], Bukkit.getPluginManager().getPlugin(module[0]).getDescription().getVersion(), module[1]));
                    throwMessage("warning", String.format(plugin.lang.getForKey("messages.disableIntegration"), module[2]));
                }
            }
        } catch (ClassNotFoundException ex) {
            throwMessage("severe", String.format(plugin.lang.getForKey("messages.integrationUnsupported"), plugin));
        } catch (InstantiationException | IllegalAccessException ex) {
            throwMessage("severe", String.format(plugin.lang.getForKey("messages.integrationUnsupportedShouldBe"), plugin));
            ex.printStackTrace();
        }
        
    }
    
    public List<String> convertToModule(String pluginToUse) {
        String name = Bukkit.getPluginManager().getPlugin(pluginToUse).getName();
        String version = Bukkit.getPluginManager().getPlugin(pluginToUse).getDescription().getVersion();
        for (List<String> module : plugin.availableIntergrations) {
            if (module.get(0).equals(name) && version.startsWith(module.get(1))) {
                return module;
            }
        }
        return null;
    }
    public boolean isEnabledIntegration(List<String> pluginToUse) {
        for (Integration integration : plugin.loadedIntegrations) {
            if (integration.getPluginName().equals(pluginToUse.get(0)) && integration.getPluginVersion().startsWith(pluginToUse.get(1))) {
                return true;
            }
        }
        return false;
    }
    
    public void disableIntegrationFor(List<String> PluginToDisable) {
        Integration toDisable = null;
        for (Integration integration : plugin.loadedIntegrations) {
            if (integration.getPluginName().equals(PluginToDisable.get(0)) && integration.getPluginVersion().startsWith(PluginToDisable.get(1))) {
                toDisable = integration;
            }
        }
        if (toDisable != null) {
            plugin.loadedIntegrations.remove(toDisable);
        }
    }
    public void loadIntegrations() {
        for (List<String> p : plugin.availableIntergrations) {
            loadIntegrationFor(p);
        }
    }
    
    public long convertMsToSecond(long newMS, long oldMS) {
        return ((newMS - oldMS) / 1000);
    }
    
    @Override
    void loadData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void saveData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
