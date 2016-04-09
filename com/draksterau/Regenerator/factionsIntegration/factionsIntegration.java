/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.factionsIntegration;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


/**
 *
 * @author draks
 */
public class factionsIntegration {
    
    public static Faction getFactionForChunk(Chunk chunk) {
        PS psChunk = PS.valueOf(chunk);
        Faction factionAtChunk = BoardColl.get().getFactionAt(psChunk);
        return factionAtChunk;
    }
    
    public static boolean factionExists(String name) {
        Faction faction = FactionColl.get().getByName(name);
        return faction instanceof Faction;
    }
    public static void regenerateChunk(String worldName, int x, int z) {
        World world = Bukkit.getServer().getWorld(worldName); 
        Location location;
        location = new Location(world, (double)x, 100.0, (double)z);
        Chunk chunk = world.getChunkAt(location); 
        chunk.getWorld().regenerateChunk(x, z);
    }
    
    public String getRelationBetweenFactions(String faction1Name, String faction2Name) {
        Faction faction2 = FactionColl.get().getByName(faction2Name);
        Rel relation = FactionColl.get().getByName(faction1Name).getRelationTo(faction2);
        return relation.getName();
    }

    public static void tellPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
    public String getFactionForPlayer(String name) {
       Player player = getBukkitPlayer(name);
       if (player != null) {
            MPlayer mplayer = MPlayer.get(player);
            return mplayer.getFactionName();
       } else {
           System.out.println("Coult not find bukkit player: " + name);
           return null;
       }
    }
    public Player getBukkitPlayer(String name) {
        return Bukkit.getPlayer(name);
    }
}
