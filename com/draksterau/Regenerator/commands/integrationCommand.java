/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import org.bukkit.ChatColor;

/**
 *
 * @author draks
 */
public class integrationCommand {
    RegeneratorCommand command;
    
    public integrationCommand(RegeneratorCommand RegeneratorCommand) {
        this.command = RegeneratorCommand;
    }
    
    public void doCommand() {
        command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GOLD + "Listing Integrations....");
        for (String integration : command.plugin.availableIntergrations) {
            if (command.plugin.isEnabledIntegration(integration)) {
                command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GRAY + integration + ChatColor.GRAY + ": " + ChatColor.GREEN + " Active");
            } else {
                command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GRAY + integration + ChatColor.GRAY + ": " + ChatColor.RED + " Inactive");
            }
        }
        command.sender.sendMessage(command.plugin.getFancyName() + ChatColor.GOLD + "If you wish to see a plugin supported that is not listed here, talk to " + command.plugin.getDescription().getAuthors().get(0) + " on Discord or on SpigotMC.org!");
    }
}
