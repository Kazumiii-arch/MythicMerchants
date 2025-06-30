package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Manages all administrative GUIs, starting with the main dashboard.
 */
public class AdminGUIManager {

    public static final String ADMIN_DASHBOARD_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Mythic Merchants - Admin";
    private final MythicMerchants plugin;

    public AdminGUIManager(MythicMerchants plugin) {
        this.plugin = plugin;
    }

    public void openAdminDashboard(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ADMIN_DASHBOARD_TITLE);

        // Core Management Features
        gui.setItem(10, createGuiItem(Material.CHEST,
                "&6Merchant Profiles",
                "&7View, create, and edit all",
                "&7available merchant types."));

        gui.setItem(12, createGuiItem(Material.COMPARATOR,
                "&aGlobal Settings",
                "&7Modify plugin-wide settings like",
                "&7the server tip interval via a GUI."));

        gui.setItem(14, createGuiItem(Material.BEACON,
                "&bSpawn a Merchant",
                "&7Select a profile and spawn a",
                "&7merchant at your location."));
                
        gui.setItem(16, createGuiItem(Material.ITEM_FRAME,
                "&dCustom Currency Tools",
                "&7Get the data string for an item",
                "&7or give currency to players."));

        // Diagnostics & Info
        gui.setItem(26, createGuiItem(Material.BOOK,
                "&cPlugin Status & Reload",
                "&7View diagnostic info and",
                "&7reload the plugin configuration."));
        
        Economy econ = MythicMerchants.getEconomy();
        ItemStack vaultStatus;
        if (econ != null) {
            vaultStatus = createGuiItem(Material.LIME_DYE, "&aVault Hooked", "&7Economy features are enabled.");
        } else {
            vaultStatus = createGuiItem(Material.GRAY_DYE, "&cVault Not Found", "&7Economy features are disabled.");
        }
        gui.setItem(18, vaultStatus);

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> loreList = Arrays.asList(lore);
            loreList.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}
