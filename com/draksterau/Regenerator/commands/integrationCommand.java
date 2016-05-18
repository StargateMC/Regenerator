/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.commands;

import java.util.List;
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
        command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GOLD + "Listing Integrations....");
        for (List<String> integration : command.plugin.availableIntergrations) {
            String[] integrationArray = integration.toArray(new String[integration.size()]);
            if (command.plugin.utils.isEnabledIntegration(integration)) {
                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GRAY + integrationArray[2] + ChatColor.GRAY + ": " + ChatColor.GREEN + " Active");
            } else {
                command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GRAY + integrationArray[2] + ChatColor.GRAY + ": " + ChatColor.RED + " Inactive");
            }
        }
        command.sender.sendMessage(command.plugin.utils.getFancyName() + ChatColor.GOLD + "If you wish to see a plugin supported that is not listed here, talk to " + command.plugin.getDescription().getAuthors().get(0) + " on Discord or on SpigotMC.org!");
    }
}
