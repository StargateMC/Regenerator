/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import com.draksterau.Regenerator.config.chunkConfigHandler;
import com.draksterau.Regenerator.config.worldConfigHandler;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;

/**
 *
 * @author draks
 */
public class infoCommand {
    
    RegeneratorCommand command;
    
    public infoCommand(RegeneratorCommand RegeneratorCommand) {
        this.command = RegeneratorCommand;
    }
    
    public void doCommand() {
        if (command.args.length == 2) {
                World world = command.plugin.getSenderChunk(command.sender).getWorld();
                Chunk chunk = command.plugin.getSenderChunk(command.sender);
                chunkConfigHandler cConfig = new chunkConfigHandler(command.plugin, chunk);
                worldConfigHandler wConfig = new worldConfigHandler(command.plugin, world);
            switch (command.args[1]) {
                case "world":
                command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GOLD + "Loading World information....");
                command.sender.sendMessage(command.plugin.getFancyName() + " World Allows AutoRegen: " + wConfig.getAutoRegen());
                command.sender.sendMessage(command.plugin.getFancyName() + " World Allows ManualRegen: " + wConfig.getManualRegen());
                command.sender.sendMessage(command.plugin.getFancyName() + " World Regeneration Interval (secs): " + wConfig.getInterval());
                command.sender.sendMessage(command.plugin.getFancyName() + " World Skip-radius: " + wConfig.getSkipRadius());
                long lastActionMins = (command.plugin.convertMsToSecond(System.currentTimeMillis(), wConfig.getLastAction()) / 60);
                if (wConfig.getLastAction() == 0) {
                    command.sender.sendMessage(command.plugin.getFancyName() + " Last Action: " + ChatColor.RED + "Never");
                } else {
                    command.sender.sendMessage(command.plugin.getFancyName() + " Last Action: " + lastActionMins + " mins ago");
                }
                break;
                case "chunk":
                    long lastRegenMins = (command.plugin.convertMsToSecond(System.currentTimeMillis(), cConfig.getLastRegen()) / 60);
                    long lastBreakMins = (command.plugin.convertMsToSecond(System.currentTimeMillis(), cConfig.getLastBroken()) / 60);
                    long lastPlaceMins = (command.plugin.convertMsToSecond(System.currentTimeMillis(), cConfig.getLastPlaced()) / 60);
                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GOLD + "Loading Chunk information....");
                    if (cConfig.getLastPlaced() == 0) {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Block Place: " + ChatColor.RED + "Never");
                    } else {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Block Place: " + lastPlaceMins + " mins ago");
                    }
                    if (cConfig.getLastBroken() == 0) {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Block Break: " + ChatColor.RED + "Never");
                    } else {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Block Break: " + lastBreakMins + " mins ago");
                    }
                    if (cConfig.getLastRegen() == 0) {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Regen: " + ChatColor.RED + "Never");
                    } else {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Last Regen: " + lastRegenMins + " mins ago");
                    }
                    command.sender.sendMessage(command.plugin.getFancyName() + " Manual Regen Allowed: " + cConfig.getManualRegen());
                    command.sender.sendMessage(command.plugin.getFancyName() + " Auto Regen Allowed: " + cConfig.getAutoRegen());
                    if (command.plugin.getIntegrationForChunk(chunk) != null) {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Protected by: " + ChatColor.BLUE + command.plugin.getIntegrationForChunk(chunk).getPluginName());
                    } else {
                        command.sender.sendMessage(command.plugin.getFancyName() + " Protected by: " + ChatColor.RED + "None");
                    }
                    if (wConfig.getManualRegen()) {
                        if (cConfig.getManualRegen()) {
                            if (command.plugin.canManuallyRegen(command.plugin.getSenderPlayer(command.sender), chunk)) {
                                if (command.plugin.getIntegrationForChunk(chunk) == null) {
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GREEN + "You can regenerate this unclaimed chunk manually");
                                } else {
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GREEN + "You can regenerate this " + command.plugin.getIntegrationForChunk(chunk).getPluginName() + " protected chunk");
                                }
                            } else {
                                if (command.plugin.getIntegrationForChunk(chunk) == null) {
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "You cannot regenerate this unclaimed chunk.");
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "This requires the regenerator.regen.unclaimed permission node.");
                                } else {
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "You cannot regenerate this " + command.plugin.getIntegrationForChunk(chunk).getPluginName() + " protected chunk");
                                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "Type /regenerator regen to find out what permission node is required.");
                                }
                            }
                        } else {
                            command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "You cannot regenerate this chunk as the chunk has manual regeneration disabled.");
                        }
                    } else {
                        command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "You cannot regenerate this chunk as the world has manual regeneration disabled.");
                    }
                    break;
                default:
                    command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "Error.. You must specific either world or chunk! (eg. /regenerator info chunk)");
                    break;
            }
        } else {
            command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.RED + "Error.. You must specific either world or chunk! (eg. /regenerator info chunk)");
        }
    }
}