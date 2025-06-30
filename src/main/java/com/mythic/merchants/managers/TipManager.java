package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TipManager {

    private final MythicMerchants plugin;
    private BukkitTask tipTask;
    private List<String> tips;
    private String prefix;
    private int interval;
    private int currentTipIndex = 0;

    public TipManager(MythicMerchants plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    private void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("server-tips.prefix", "&e&l[TIP] &r&7"));
        this.tips = config.getStringList("server-tips.messages");
        this.interval = config.getInt("server-tips.interval", 600);
    }

    public void startTipTask() {
        if (tipTask != null) {
            tipTask.cancel();
        }
        if (!plugin.getConfig().getBoolean("server-tips.enabled", false) || tips.isEmpty()) {
            return;
        }
        long delay = interval * 20L;
        tipTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String message = tips.get(currentTipIndex);
            Bukkit.broadcastMessage(prefix + message);
            currentTipIndex = (currentTipIndex + 1) % tips.size();
        }, delay, delay);
    }
    
    public void reloadAndRestart() {
        loadConfigValues();
        startTipTask();
    }
}
