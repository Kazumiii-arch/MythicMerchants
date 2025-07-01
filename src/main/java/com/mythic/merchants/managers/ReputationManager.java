package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ReputationManager {

    private final MythicMerchants plugin;
    private File playerDataFile;
    private FileConfiguration playerDataConfig;
    private final Map<UUID, Map<String, Integer>> reputationCache = new HashMap<>();
    private final Map<String, Integer> tierThresholds = new LinkedHashMap<>();

    public ReputationManager(MythicMerchants plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        setupFiles();
        loadTiers();
        loadAllPlayerData();
    }

    private void setupFiles() {
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        
        playerDataFile = new File(dataDir, "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }
    
    private void loadTiers() {
        tierThresholds.clear();
        ConfigurationSection tierSection = plugin.getConfig().getConfigurationSection("reputation.tiers");
        if(tierSection != null) {
            tierSection.getValues(false).entrySet().stream()
                .sorted(Map.Entry.<String, Object>comparingByValue().reversed())
                .forEach(entry -> tierThresholds.put(entry.getKey().toUpperCase(), (Integer) entry.getValue()));
        }
    }

    public void loadAllPlayerData() {
        reputationCache.clear();
        ConfigurationSection playersSection = playerDataConfig.getConfigurationSection("players");
        if (playersSection == null) return;
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                ConfigurationSection repSection = playersSection.getConfigurationSection(uuidString + ".reputation");
                if (repSection != null) {
                    Map<String, Integer> playerReps = new HashMap<>();
                    for (String faction : repSection.getKeys(false)) {
                        playerReps.put(faction.toUpperCase(), repSection.getInt(faction));
                    }
                    reputationCache.put(uuid, playerReps);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidString);
            }
        }
    }

    public void saveData() {
        playerDataConfig.set("players", null);
        for (Map.Entry<UUID, Map<String, Integer>> playerEntry : reputationCache.entrySet()) {
            for (Map.Entry<String, Integer> repEntry : playerEntry.getValue().entrySet()) {
                playerDataConfig.set("players." + playerEntry.getKey().toString() + ".reputation." + repEntry.getKey(), repEntry.getValue());
            }
        }
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data to playerdata.yml", e);
        }
    }
    
    public Map<String, Integer> getPlayerReputationMap(UUID uuid) {
        return reputationCache.getOrDefault(uuid, new HashMap<>());
    }

    public int getReputation(UUID uuid, String faction) {
        if (faction == null) return 0;
        return reputationCache.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(faction.toUpperCase(), 0);
    }
    
    public void addReputation(UUID uuid, String faction, int amount) {
        if(faction == null || faction.isEmpty()) return;
        Map<String, Integer> playerReps = reputationCache.computeIfAbsent(uuid, k -> new HashMap<>());
        int currentRep = playerReps.getOrDefault(faction.toUpperCase(), 0);
        playerReps.put(faction.toUpperCase(), currentRep + amount);
    }
    
    public String getTier(int reputationPoints) {
        for(Map.Entry<String, Integer> entry : tierThresholds.entrySet()) {
            if(reputationPoints >= entry.getValue()) {
                return entry.getKey();
            }
        }
        return "NEUTRAL";
    }

    public boolean hasRequiredReputation(Player player, String faction, String requiredTierName) {
        if (requiredTierName == null || requiredTierName.isEmpty() || faction == null || faction.isEmpty()) {
            return true;
        }
        int playerRep = getReputation(player.getUniqueId(), faction);
        int requiredPoints = tierThresholds.getOrDefault(requiredTierName.toUpperCase(), Integer.MAX_VALUE);
        return playerRep >= requiredPoints;
    }
            }
