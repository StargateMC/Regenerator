/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import com.draksterau.Regenerator.Handlers.RChunk;
import com.draksterau.Regenerator.Handlers.RWorld;
import org.bukkit.Bukkit;
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
                World world = command.plugin.utils.getSenderChunk(command.sender).getWorld();
                RWorld rWorld = new RWorld(command.plugin, world);
                Chunk chunk = command.plugin.utils.getSenderChunk(command.sender);
                RChunk rChunk = new RChunk(command.plugin, chunk.getX(), chunk.getZ(), world.getName());
            switch (command.args[1]) {
                case "world":
                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GOLD + "Loading World information....");
                command.sender.sendMessage(command.plugin.utils.getFancyName() + "AutoRegen: " + command.plugin.utils.getStatusForBoolean(rWorld.canAutoRegen()));
                command.sender.sendMessage(command.plugin.utils.getFancyName() + "ManualRegen: " + command.plugin.utils.getStatusForBoolean(rWorld.canManualRegen()));
                command.sender.sendMessage(command.plugin.utils.getFancyName() + "Regeneration Interval: " + rWorld.getFormattedInterval());
                if (command.plugin.isBacklogged()) {
                    command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "Regeneration queue is currently backlogged. It will catch up in approximately " + command.plugin.utils.getEnglishTimeFromMs((long)Math.floor(command.plugin.parseQueue() * command.plugin.config.parseInterval) * 1000) + " (" + Math.ceil(command.plugin.parseQueue()) + " executions of the Regeneration Task).");
                }
                break;
                case "chunk":
                    long TimeToRegenSecs = 0;
                    long timeSinceRegenSecs = (command.plugin.utils.convertMsToSecond(System.currentTimeMillis(), rChunk.lastActivity));
                    boolean isBacklogged = command.plugin.isBacklogged();  
                    double parsesToComplete = command.plugin.parseQueue();
                    long includingQueueTimeOf = command.plugin.getQueueDelay() + rWorld.regenInterval;
                    if (rWorld.regenInterval > timeSinceRegenSecs) {
                        TimeToRegenSecs = (rWorld.regenInterval - timeSinceRegenSecs);
                    }
                    includingQueueTimeOf = (long) Math.floor((rWorld.regenInterval - timeSinceRegenSecs) + includingQueueTimeOf);
                    command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GOLD + "Loading Chunk information....");
                    if (rChunk.lastActivity == 0 || rChunk.lastActivity == -1) {
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + " Last Activity: " + ChatColor.RED + "Never / Untracked");
                    } else {
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + " Last Activity: " + (command.plugin.utils.getEnglishTimeFromMs(timeSinceRegenSecs * 1000)));
                        if (TimeToRegenSecs == 0 && rWorld.autoRegen) {
                            command.sender.sendMessage(command.plugin.utils.getFancyName() + " Flagged for regen: " + ChatColor.RED + "Now");
                            command.sender.sendMessage(command.plugin.utils.getFancyName() + " Regen should occur before: " + ChatColor.RED + (command.plugin.utils.getEnglishTimeFromMs(includingQueueTimeOf * 1000)));
                        } else {
                            if (rWorld.autoRegen) command.sender.sendMessage(command.plugin.utils.getFancyName() + " Flagged for regen: " + (command.plugin.utils.getEnglishTimeFromMs(TimeToRegenSecs * 1000)));
                            command.sender.sendMessage(command.plugin.utils.getFancyName() + " Regen should occur before: " + ChatColor.RED + (command.plugin.utils.getEnglishTimeFromMs(includingQueueTimeOf * 1000)));
                        }
                    }
                    if (command.plugin.config.enableUnknownProtectionDetection && !command.plugin.utils.canBreakChunk(rChunk.getChunk())) {
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + " Protected by: " + ChatColor.BLUE + "Unknown Plugin");
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "UnknownProtectionDetection is protecting this chunk from regeneration. This overrides all integrations.");
                    } else {
                        if (command.plugin.utils.getIntegrationForChunk(chunk) != null) {
                            command.sender.sendMessage(command.plugin.utils.getFancyName() + " Protected by: " + ChatColor.BLUE + command.plugin.utils.getIntegrationForChunk(chunk).getPluginName());
                        } else {
                            command.sender.sendMessage(command.plugin.utils.getFancyName() + " Protected by: " + ChatColor.RED + "None");
                        }
                    }
                    if (isBacklogged) {
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "Regeneration queue is currently backlogged. It will catch up in approximately " + command.plugin.utils.getEnglishTimeFromMs((long)Math.floor(parsesToComplete * command.plugin.config.parseInterval) * 1000) + " (" + Math.ceil(parsesToComplete) + " executions of the Regeneration Task).");
                    }
                    if (rChunk.canManualRegen()) {
                        if (command.plugin.utils.canManuallyRegen(command.plugin.utils.getSenderPlayer(command.sender), chunk)) {
                            if (command.plugin.utils.getIntegrationForChunk(chunk) == null) {
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GREEN + "You can regenerate this unclaimed chunk manually");
                            } else {
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GREEN + "You can regenerate this " + command.plugin.utils.getIntegrationForChunk(chunk).getPluginName() + " protected chunk");
                            }
                        } else {
                            if (command.plugin.utils.getIntegrationForChunk(chunk) == null) {
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "You cannot regenerate this unclaimed chunk.");
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "This requires the regenerator.regen.unclaimed permission node.");
                            } else {
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "You cannot regenerate this " + command.plugin.utils.getIntegrationForChunk(chunk).getPluginName() + " protected chunk");
                                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "Type /regenerator regen to find out what permission node is required.");
                            }
                        }
                    } else {
                        command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "You cannot regenerate this chunk as the world has manual regeneration disabled.");
                    }
                    if (!rWorld.autoRegen) command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "This chunk will not automatically regenerate as the world has automated regeneration disabled!");
                    break;
                default:
                    command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "Error.. You must specific either world or chunk! (eg. /regenerator info chunk)");
                    break;
            }
        } else {
            command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.RED + "Error.. You must specific either world or chunk! (eg. /regenerator info chunk)");
        }
    }
}