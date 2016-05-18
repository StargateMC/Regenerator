/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stargatemc.SRegen.commands;

import com.stargatemc.SRegen.handlers.RWorld;
import com.stargatemc.SRegen.tasks.regenTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

/**
 *
 * @author draks
 */
public class reloadCommand {
    
    SRegenCommand command;
    
    public reloadCommand(SRegenCommand SRegenCommand) {
        this.command = SRegenCommand;
    }
    
    public void doCommand() {
       command.sender.sendMessage(ChatColor.GRAY + "Unloading integrations...");
       command.plugin.availableIntergrations.clear();
       command.plugin.loadedIntegrations.clear();
       command.sender.sendMessage(ChatColor.GRAY + "Cancelling all regen tasks...");
        Bukkit.getScheduler().cancelTasks(command.plugin);
        command.plugin.onEnable();
        if (command.plugin.isEnabled()) {
            command.sender.sendMessage(ChatColor.GREEN + "SRegen has been reloaded!");
        } else {
            command.sender.sendMessage(ChatColor.RED + "SRegen failed to reload, a restart may be required!");
        }
    }
}
