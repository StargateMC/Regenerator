/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import com.draksterau.Regenerator.Handlers.RWorld;
import com.draksterau.Regenerator.tasks.regenTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

/**
 *
 * @author draks
 */
public class reloadCommand {
    
    RegeneratorCommand command;
    
    public reloadCommand(RegeneratorCommand RegeneratorCommand) {
        this.command = RegeneratorCommand;
    }
    
    public void doCommand() {
       command.sender.sendMessage(ChatColor.GRAY + "Unloading integrations...");
       command.plugin.availableIntergrations.clear();
       command.plugin.loadedIntegrations.clear();
       command.sender.sendMessage(ChatColor.GRAY + "Cancelling all regen tasks...");
        Bukkit.getScheduler().cancelTasks(command.plugin);
        command.plugin.onEnable();
        if (command.plugin.isEnabled()) {
            command.sender.sendMessage(ChatColor.GREEN + "Regenerator has been reloaded!");
        } else {
            command.sender.sendMessage(ChatColor.RED + "Regenerator failed to reload, a restart may be required!");
        }
    }
}
