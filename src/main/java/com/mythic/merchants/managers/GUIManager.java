package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.objects.MerchantTrade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GUIManager {

    public static final String TRADE_GUI_TITLE_PREFIX = ChatColor.DARK_GREEN + "Trades: ";
    private final MythicMerchants plugin;
    private final MerchantManager merchantManager;

    public GUIManager(MythicMerchants plugin) {
        this.plugin = plugin;
        this.merchantManager = plugin.getMerchantManager();
    }

    public void openTradeGUI(Player player, String profileId) {
        ConfigurationSection profile = merchantManager.getProfileConfig(profileId);
        if (profile == null) return;

        String guiTitle = TRADE_GUI_TITLE_PREFIX + ChatColor.stripColor(profile.getString("settings.display-name", "Merchant"));
        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

        List<MerchantTrade> availableTrades = getAvailableTrades(profileId);

        for (int i = 0; i < availableTrades.size() && i < 54; i++) {
            gui.setItem(i, availableTrades.get(i).getDisplayItem(player));
        }

        player.openInventory(gui);
    }

    public List<MerchantTrade> getAvailableTrades(String profileId) {
        List<MerchantTrade> trades = new ArrayList<>();
        ConfigurationSection profile = merchantManager.getProfileConfig(profileId);
        if (profile == null) return trades;
        
        ConfigurationSection tradePool = profile.getConfigurationSection("trade-pool");
        if (tradePool == null) return trades;

        String faction = profile.getString("settings.faction");

        for (String key : tradePool.getKeys(false)) {
            ConfigurationSection tradeSection = tradePool.getConfigurationSection(key);
            if (tradeSection == null) continue;
            
            double chance = tradeSection.getDouble("chance", 1.0);
            if (new Random().nextDouble() > chance) continue;

            try {
                // Pass the profileId to the MerchantTrade constructor
                MerchantTrade trade = new MerchantTrade(plugin, key, tradeSection, faction, profileId);
                trades.add(trade);
            } catch (IOException | IllegalArgumentException e) {
                plugin.getLogger().warning("Failed to load trade '" + key + "' for merchant '" + profileId + "'. Invalid result item or format.");
            }
        }
        return trades;
    }
}
