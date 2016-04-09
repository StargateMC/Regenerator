package com.draksterau.Regenerator.tasks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.draksterau.Regenerator.factionsIntegration.factionsIntegration;
import com.draksterau.Regenerator.threads.RegenThread;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;



public class RegenTask extends BukkitRunnable {
    
    Plugin plugin;
    
    private Logger log = Logger.getLogger("Minecraft");
    
    // This is used to determine if we should regenerate or not.

    public RegenTask (Plugin plugin) {
        this.plugin = plugin;
    }

    
    @Override
    public void run() {        
            Thread RegenThread = new RegenThread(plugin);
            RegenThread.start();
    }	
}

