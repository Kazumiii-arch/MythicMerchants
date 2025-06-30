package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EconomyManager {

    private final MythicMerchants plugin;
    private File economyFile;
    private FileConfiguration economyConfig;

    private final Map<String, Double> currentPrices = new HashMap<>();
    private final Map<String, Integer> globalStock = new HashMap<>();

    public EconomyManager(MythicMerchants plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        setupFiles();
        loadData();
    }

    private void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        
        economyFile = new File(dataDir, "economy.yml");
        if (!economyFile.exists()) {
            try {
                economyFile.createNewFile();
            } catch (IOException e) {
                 plugin.getLogger().log(Level.SEVERE, "Could not create economy.yml", e);
            }
        }
        economyConfig = YamlConfiguration.loadConfiguration(economyFile);
    }

    public void loadData() {
        currentPrices.clear();
        globalStock.clear();
        
        ConfigurationSection trackedItems = plugin.getConfig().getConfigurationSection("dynamic-economy.tracked-items");
        if (trackedItems == null) return;

        ConfigurationSection dynamicItemsData = economyConfig.getConfigurationSection("dynamic-items");
        if (dynamicItemsData == null) {
            dynamicItemsData = economyConfig.createSection("dynamic-items");
        }

        for (String key : trackedItems.getKeys(false)) {
            if (trackedItems.contains(key + ".base-price")) {
                double basePrice = trackedItems.getDouble(key + ".base-price");
                double currentPrice = dynamicItemsData.getDouble(key + ".current-price", basePrice);
                currentPrices.put(key.toUpperCase(), currentPrice);
            }
            if (trackedItems.contains(key + ".global-stock")) {
                int defaultStock = trackedItems.getInt(key + ".global-stock");
                int currentStock = dynamicItemsData.getInt(key + ".current-global-stock", defaultStock);
                globalStock.put(key.toUpperCase(), currentStock);
            }
        }
    }

    public void saveData() {
        ConfigurationSection dynamicItemsData = economyConfig.isConfigurationSection("dynamic-items") ?
                economyConfig.getConfigurationSection("dynamic-items") : economyConfig.createSection("dynamic-items");

        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            dynamicItemsData.set(entry.getKey().toUpperCase() + ".current-price", entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : globalStock.entrySet()) {
            dynamicItemsData.set(entry.getKey().toUpperCase() + ".current-global-stock", entry.getValue());
        }
        
        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save economy data to economy.yml", e);
        }
    }
    
    public void startRestockTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ConfigurationSection trackedItems = plugin.getConfig().getConfigurationSection("dynamic-economy.tracked-items");
                if(trackedItems == null) return;

                for(String key : trackedItems.getKeys(false)) {
                    int restockInterval = trackedItems.getInt(key + ".restock-interval", -1);
                    if(restockInterval > 0) {
                        int currentStock = globalStock.getOrDefault(key.toUpperCase(), 0);
                        int maxStock = trackedItems.getInt(key + ".global-stock");
                        if(currentStock < maxStock) {
                            globalStock.put(key.toUpperCase(), currentStock + 1);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 300, 20L * 300); // Check every 5 minutes
    }

    public int getGlobalStock(String itemId) {
        return globalStock.getOrDefault(itemId.toUpperCase(), -1);
    }

    public void decrementGlobalStock(String itemId) {
        String key = itemId.toUpperCase();
        if(globalStock.containsKey(key)) {
            globalStock.put(key, Math.max(0, globalStock.get(key) - 1));
        }
    }
}
