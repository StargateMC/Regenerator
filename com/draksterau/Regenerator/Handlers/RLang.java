/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RLang extends RObject {

    private File langConfigFile;
    private FileConfiguration langConfig;
    public RLang(RegeneratorPlugin plugin) {
        super(plugin);
        this.loadData();
    }
    @Override
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
            return "ERROR - Key: " + key + " does not exist in the localisation file!";
        } else {
            return value;
        }
    }
    
    @Override
    void saveData() {
        try {
            langConfig.save(langConfigFile);
        } catch (IOException ex) {
            plugin.utils.throwMessage("severe","Could not save localisation config to " + langConfigFile.getAbsolutePath() + " (Exception: " + ex.getMessage() + ")");
        }    
    }

}
