/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Utils;

import com.draksterau.Regenerator.Regenerator;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Allison
 */
public final class LangUtils {
    
    private File langConfigFile;
    private FileConfiguration langConfig;
    private File langConfigFileTemp;
    private FileConfiguration langConfigTemp;
    public JavaPlugin plugin;
    
    public LangUtils(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }
    
    void loadData() {
        // Attempt to load the config file.
        langConfigFile = new File(plugin.getDataFolder() + "/messages.yml");
        // Attempt to read the config in the config file.
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        // If the file doesnt exist, populate it from the template.
        if (!langConfigFile.exists()) {
            langConfigFile = new File(plugin.getDataFolder() + "/messages.yml");
            langConfig = YamlConfiguration.loadConfiguration(plugin.getResource("messages.yml"));
            saveData();
        }
    }

    public String getForKey(String key) {
        String value = langConfig.getString(key);
        if (value == null) {
            langConfigFileTemp = new File(plugin.getDataFolder() + "/messages.yml");
            langConfigTemp = YamlConfiguration.loadConfiguration(plugin.getResource("messages.yml"));
            if (langConfigTemp.isSet(key)) {
                Regenerator.getInstance().getUtils().throwMessage(plugin, "new","Loading default value for " + key + " as it does not exist in messages.yml!");
                String tempValue = langConfigTemp.getString(key);
                langConfig.set(key, tempValue);
                this.saveData();
                return tempValue;
            } else {
                Regenerator.getInstance().getUtils().throwMessage(plugin, "warning", "ERROR - Key: " + key + " does not exist in the localisation file. Setting to default of UNDEFINED.");
                langConfig.set(key, "UNDEFINED");
                saveData();
                return "UNDEFINED";
            }
        } else {
            return value;
        }
    }
    
    void saveData() {
        try {
            langConfig.save(langConfigFile);
        } catch (IOException ex) {
            Regenerator.getInstance().getUtils().throwMessage(plugin, "severe","Could not save localisation config to " + langConfigFile.getAbsolutePath() + " (Exception: " + ex.getMessage() + ")");
        }    
    }
}
