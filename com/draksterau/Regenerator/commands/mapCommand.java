/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.RegeneratorPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author draks
 */
public class mapCommand {
    
    RegeneratorCommand command;
    
    public mapCommand(RegeneratorCommand RegeneratorCommand) {
        this.command = RegeneratorCommand;
    }
    
    public boolean doCommand() {
        if (command.plugin.utils.getSenderPlayer(command.sender) == null) {
            command.sender.sendMessage(ChatColor.RED + "This command can only be performed while in-game.");
        } else {
            Chunk rootChunk = command.plugin.utils.getSenderPlayer(command.sender).getLocation().getChunk();
            int x;
            int z;

            command.sender.sendMessage(ChatColor.GOLD + "Legend: " + ChatColor.BLUE + "O" + ChatColor.GRAY + ": Pending AutoRegen, " + ChatColor.RED + "X" + ChatColor.GRAY + ": Cant Manual Regen, " + ChatColor.GREEN + "#" + ChatColor.GRAY + ": Can Manual Regen Unclaimed, " +ChatColor.GREEN + "*" + ChatColor.GRAY + " Can Manual Regen Claimed" + ChatColor.DARK_PURPLE + "$" + ChatColor.GRAY + " Conflicted by multiple plugins"); 
            command.sender.sendMessage(ChatColor.GOLD + "-----------");
            String string = ChatColor.GOLD + "[";
            for (x = (rootChunk.getX()-64); x <= (rootChunk.getX() + 64); x = x+16) {
                for (z = (rootChunk.getZ()-64); z <= (rootChunk.getZ() + 64); z = z+16) {
                    Chunk chunk = command.plugin.utils.getSenderPlayer(command.sender).getLocation().getWorld().getChunkAt(x, z);
                    if (command.plugin.utils.getCountIntegration(chunk) < 2) {
        
                        RChunk rChunk = new RChunk(command.plugin, chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
                        if (command.plugin.utils.autoRegenRequirementsMet(chunk) && rChunk.lastActivity != 0) {
                            string = string + ChatColor.BLUE + "O";
                        } else {
                            if (command.plugin.utils.canManuallyRegen(command.plugin.utils.getSenderPlayer(command.sender), chunk) && command.plugin.utils.getIntegrationForChunk(chunk) == null) {
                                string = string + ChatColor.GREEN + "#";
                            } else {
                                if (command.plugin.utils.getIntegrationForChunk(chunk) != null) {
                                    if (command.plugin.utils.getIntegrationForChunk(chunk).canPlayerRegen(command.plugin.utils.getSenderPlayer(command.sender), chunk)) {
                                        string = string + ChatColor.GREEN + "*";
                                    } else {
                                        string = string + ChatColor.RED + "X";
                                    }
                                } else {
                                    string = string + ChatColor.GRAY + "/";
                                }
                            }
                        }
                    } else {
                        string = string + ChatColor.DARK_PURPLE + "$";
                    }
                }
                command.sender.sendMessage(string + ChatColor.GOLD + "]");
                string = ChatColor.GOLD + "[";                
            }
            command.sender.sendMessage(ChatColor.GOLD + "-----------");
        }
        return true;
    }
}
