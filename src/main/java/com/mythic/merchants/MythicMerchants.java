package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MerchantManager {

    private final MythicMerchants plugin;
    private final Map<String, FileConfiguration> merchantProfiles = new HashMap<>();
    private final Map<UUID, String> activeMerchants = new HashMap<>();
    private File permanentMerchantFile;
    private FileConfiguration permanentMerchantData;
    private final NamespacedKey merchantKey;
    private BukkitTask spawnTask;

    public MerchantManager(MythicMerchants plugin) {
        this.plugin = plugin;
        this.merchantKey = new NamespacedKey(plugin, "merchant_profile_id");
        reloadAllProfiles();
        loadPermanentMerchantData();
    }
    
    public void reloadAllProfiles() {
        merchantProfiles.clear();
        File merchantsDir = new File(plugin.getDataFolder(), "merchants");
        if (!merchantsDir.exists() || merchantsDir.listFiles() == null) return;
        for (File file : merchantsDir.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                String profileId = file.getName().replace(".yml", "");
                merchantProfiles.put(profileId, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    private void loadPermanentMerchantData() {
        permanentMerchantFile = new File(plugin.getDataFolder(), "data/merchants.yml");
        if (!permanentMerchantFile.exists()) {
            try { permanentMerchantFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        permanentMerchantData = YamlConfiguration.loadConfiguration(permanentMerchantFile);
    }
    
    public void loadPermanentMerchants() {
        ConfigurationSection section = permanentMerchantData.getConfigurationSection("permanent-merchants");
        if (section == null) return;
        for (String uuidString : section.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
            
            String profileId = section.getString(uuidString + ".profileId");
            World world = Bukkit.getWorld(section.getString(uuidString + ".world"));
            if (world == null) continue;
            
            Location loc = new Location(world,
                section.getDouble(uuidString + ".x"),
                section.getDouble(uuidString + ".y"),
                section.getDouble(uuidString + ".z"),
                (float) section.getDouble(uuidString + ".yaw"),
                (float) section.getDouble(uuidString + ".pitch"));
            
            spawnMerchant(profileId, loc, -1, true);
        }
    }

    public void startRandomSpawnTask() {
        if (spawnTask != null) spawnTask.cancel();
        if (!plugin.getConfig().getBoolean("random-spawning.enabled", true)) return;
        
        long min = plugin.getConfig().getLong("random-spawning.spawn-interval-min", 900) * 20L;
        long max = plugin.getConfig().getLong("random-spawning.spawn-interval-max", 1800) * 20L;

        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                trySpawningRandomMerchant();
            }
        }.runTaskTimer(plugin, min, max);
    }
    
    public void trySpawningRandomMerchant() {
        List<String> spawnPool = plugin.getConfig().getStringList("random-spawning.random-spawn-pool");
        if (spawnPool.isEmpty()) return;
        
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;
        
        Collections.shuffle(players);
        Collections.shuffle(spawnPool);
        
        Player targetPlayer = players.get(0);

        for (String profileId : spawnPool) {
            if (checkSpawnConditions(profileId, targetPlayer.getLocation())) {
                int duration = plugin.getConfig().getInt("defaults.duration", 600);
                spawnMerchant(profileId, findSafeLocation(targetPlayer.getLocation()), duration, false);
                return;
            }
        }
    }
    
    private boolean checkSpawnConditions(String profileId, Location location) {
        FileConfiguration profile = getProfileConfig(profileId);
        if (profile == null) return false;
        
        ConfigurationSection conditions = profile.getConfigurationSection("spawn-conditions");
        if (conditions == null) return true;

        List<String> requiredBiomes = conditions.getStringList("biomes");
        if (!requiredBiomes.isEmpty() && !requiredBiomes.contains(location.getBlock().getBiome().name())) return false;
        
        String requiredTime = conditions.getString("time", "ANY").toUpperCase();
        long worldTime = location.getWorld().getTime();
        boolean isDay = worldTime >= 0 && worldTime < 12300;
        if (requiredTime.equals("DAY") && !isDay) return false;
        if (requiredTime.equals("NIGHT") && isDay) return false;

        return true;
    }
    
    public void spawnMerchant(String profileId, Location location, int duration, boolean isPermanent) {
        FileConfiguration profile = merchantProfiles.get(profileId);
        if (profile == null) return;
        ConfigurationSection settings = profile.getConfigurationSection("settings");
        if (settings == null) return;

        EntityType type;
        try {
            type = EntityType.valueOf(settings.getString("entity-type", "VILLAGER").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid entity type for profile: " + profileId);
            return;
        }

        LivingEntity merchant = (LivingEntity) location.getWorld().spawnEntity(location, type);
        
        merchant.getPersistentDataContainer().set(merchantKey, PersistentDataType.STRING, profileId);
        String displayName = ChatColor.translateAlternateColorCodes('&', settings.getString("display-name", "Merchant"));
        merchant.setCustomName(displayName);
        merchant.setCustomNameVisible(true);
        merchant.setAI(!settings.getBoolean("is-stationary", true));
        merchant.setInvulnerable(settings.getBoolean("invulnerable", true));
        merchant.setSilent(true);
        
        activeMerchants.put(merchant.getUniqueId(), profileId);

        if (isPermanent) {
            addPermanentMerchant(merchant.getUniqueId(), profileId, location);
        } else {
            announceMerchantSpawn(displayName, location);
            if (duration > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (merchant.isValid()) merchant.remove();
                        activeMerchants.remove(merchant.getUniqueId());
                    }
                }.runTaskLater(plugin, duration * 20L);
            }
        }
    }

    private void announceMerchantSpawn(String merchantName, Location location) {
        // Implementation for announcements
    }

    private void addPermanentMerchant(UUID uuid, String profileId, Location loc) {
        String path = "permanent-merchants." + uuid.toString();
        permanentMerchantData.set(path + ".profileId", profileId);
        permanentMerchantData.set(path + ".world", loc.getWorld().getName());
        permanentMerchantData.set(path + ".x", loc.getX());
        permanentMerchantData.set(path + ".y", loc.getY());
        permanentMerchantData.set(path + ".z", loc.getZ());
        permanentMerchantData.set(path + ".yaw", loc.getYaw());
        permanentMerchantData.set(path + ".pitch", loc.getPitch());
        savePermanentData();
    }
    
    private void savePermanentData() {
        try { permanentMerchantData.save(permanentMerchantFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isMerchant(Entity entity) {
        return entity.getPersistentDataContainer().has(merchantKey, PersistentDataType.STRING);
    }
    
    public String getProfileId(Entity entity) {
        return entity.getPersistentDataContainer().get(merchantKey, PersistentDataType.STRING);
    }

    public FileConfiguration getProfileConfig(String id) {
        return merchantProfiles.get(id);
    }
    
    public void despawnAllMerchants(boolean includePermanent) {
        for(UUID uuid : new ArrayList<>(activeMerchants.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);
            if(entity != null) {
                if(!includePermanent && permanentMerchantData.isSet("permanent-merchants." + uuid.toString())) {
                    continue;
                }
                entity.remove();
            }
        }
        if (includePermanent) {
            activeMerchants.clear();
        }
    }

    private Location findSafeLocation(Location origin) {
         for (int y = 0; y < 10; y++) {
            Location loc = origin.clone().add(new Random().nextInt(16) - 8, y, new Random().nextInt(16) - 8);
            if (loc.getBlock().isPassable() && loc.clone().subtract(0, 1, 0).getBlock().isSolid()) {
                return loc.add(0.5, 0, 0.5);
            }
        }
        return origin;
    }
}
