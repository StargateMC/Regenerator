/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.config;

import com.draksterau.Regenerator.Handlers.MsgType;
import com.draksterau.Regenerator.RegeneratorPlugin;
import com.draksterau.Regenerator.integration.Integration;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class integrationConfigHandler {
    
    public FileConfiguration integrationConfig = null;
    File integrationConfigFile = null;
    public Integration integration = null;
    public RegeneratorPlugin plugin = null;
    
    public integrationConfigHandler(RegeneratorPlugin plugin, Integration integration) {
        this.integration = integration;
        this.plugin = plugin;
        getIntegrationConfig();
    }
    
    public FileConfiguration getIntegrationConfig() {
        if (integrationConfig == null) {
            reloadIntegrationConfig();
        }
        return integrationConfig;
    }

    public void configureIntegration () throws UnsupportedEncodingException {
        integrationConfig.set("enabled", "false");
        integrationConfig.set("version", "null");
        // Saves the config file.
        saveIntegrationConfig();
    }
    
    public void reloadIntegrationConfig() {
        saveDefaultIntegrationConfig();
        if (integrationConfigFile == null) {
            integrationConfigFile = new File(plugin.getDataFolder() + "/integration/" + integration.getClass().getSimpleName() + ".yml");
        }
        integrationConfig = YamlConfiguration.loadConfiguration(integrationConfigFile);

        if (integrationConfig == null) {
            try {
                configureIntegration();
            } catch (UnsupportedEncodingException ex) {
                plugin.utils.throwMessage(MsgType.SEVERE, "Failed to set default " + integration.getPluginName() + " Integration config with exception: " + ex.getMessage());
            }
        }
    }
    
    public void saveIntegrationConfig() {
        if (integrationConfig == null || integrationConfigFile == null) {
            return;
        }
        try {
            integrationConfig.save(integrationConfigFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage(MsgType.SEVERE, "Failed to save  " + integration.getPluginName() + " Integration Config with exception: " + ex.getMessage());
        }
        integrationConfig = null;
        integrationConfigFile = null;
    }
    public void saveDefaultIntegrationConfig() {
        if (integrationConfigFile == null) {
            integrationConfigFile = new File(plugin.getDataFolder() + "/integration/" + integration.getClass().getSimpleName() + ".yml");
        }
        if (!integrationConfigFile.exists()) {      
            integrationConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("integration.yml")));
            saveIntegrationConfig();
         }
    }
}
