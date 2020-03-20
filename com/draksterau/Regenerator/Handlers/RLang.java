/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.draksterau.Regenerator.Handlers;

import com.draksterau.Regenerator.RegeneratorPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author draks
 */
public final class RLang extends RObject {

    private File langConfigFile;
    private FileConfiguration langConfig;
    private File langConfigFileTemp;
    private FileConfiguration langConfigTemp;
    private String language;
    
    public RLang(RegeneratorPlugin plugin, String language) {
        super(plugin);
        this.language = language;
        this.loadData();
    }
    @Override
    void loadData() {
        // Attempt to load the config file.
        langConfigFile = new File(plugin.getDataFolder() + "lang/" + this.language + ".yml");
        // Attempt to read the config in the config file.
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        // If the config file is null (due to the config file being invalid or not there) create a new one.
        // If the file doesnt exist, populate it from the template.
        if (!langConfigFile.exists()) {
            langConfigFile = new File(plugin.getDataFolder() + "/lang/" + this.language + ".yml");
            langConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("lang/" + this.language + ".yml")));
            saveData();
        }
    }
    
    public String getLanguage() {
        return this.language;
    }
    
    public void setLanguage(String s) {
        this.language = s;
    }

    public String getForKey(String key) {
        String value = langConfig.getString(key);
        if (value == null) {
            langConfigFileTemp = new File(plugin.getDataFolder() + "/lang/" + this.language + ".yml");
            langConfigTemp = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("lang/ENGLISH.yml")));
            if (langConfigTemp.isSet(key)) {
                plugin.utils.throwMessage("new","Loading default value for " + key + " as it does not exist in " + plugin.getDataFolder() + "/lang/" + this.language + ".yml" + "!");
                String tempValue = langConfigTemp.getString(key);
                langConfig.set(key, tempValue);
                this.saveData();
                return tempValue;
            } else {
                return "ERROR - Key: " + key + " does not exist in " + plugin.getDataFolder() + "/lang/" + this.language + ".yml!";
            }
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
