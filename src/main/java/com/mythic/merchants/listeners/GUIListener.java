package com.mythic.merchants.listeners;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.AdminGUIManager;
import com.mythic.merchants.managers.GUIManager;
import com.mythic.merchants.objects.MerchantTrade;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final MythicMerchants plugin;

    public GUIListener(MythicMerchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String viewTitle = event.getView().getTitle();

        // --- Admin Dashboard Listener ---
        if (viewTitle.equals(AdminGUIManager.ADMIN_DASHBOARD_TITLE)) {
            event.setCancelled(true); // Prevent item moving
            handleAdminDashboardClick(player, event.getSlot());
            return;
        }

        // --- Player Trade GUI Listener ---
        if (viewTitle.startsWith(GUIManager.TRADE_GUI_TITLE_PREFIX)) {
            event.setCancelled(true); // Prevent item moving
            handleTradeGUIClick(player, event.getCurrentItem());
            return;
        }
    }

    private void handleAdminDashboardClick(Player player, int slot) {
        // This is where you would link to other admin GUIs.
        // As they are not built, we will just send messages for now.
        switch (slot) {
            case 10: // Merchant Profiles
                player.sendMessage(ChatColor.GOLD + "[Debug] Clicked 'Merchant Profiles'. This would open a new GUI.");
                player.closeInventory();
                break;
            case 12: // Global Settings
                player.sendMessage(ChatColor.AQUA + "[Debug] Clicked 'Global Settings'. This would open a settings editor.");
                player.closeInventory();
                break;
            case 14: // Spawn a Merchant
                player.sendMessage(ChatColor.GREEN + "[Debug] Clicked 'Spawn a Merchant'. This would open a spawn selection GUI.");
                player.closeInventory();
                break;
            case 16: // Utilities
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[Debug] Clicked 'Utilities'. This would open a utility GUI.");
                player.closeInventory();
                break;
            case 26: // Plugin Status & Reload
                plugin.reloadPlugin();
                player.sendMessage(ChatColor.GREEN + "MythicMerchants configuration has been reloaded!");
                player.closeInventory();
                break;
        }
    }

    private void handleTradeGUIClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }

        // Retrieve the trade object from the clicked item
        MerchantTrade trade = MerchantTrade.fromDisplayItem(plugin, clickedItem);

        if (trade != null) {
            // The execute method will handle all checks (reputation, cost, stock)
            trade.execute(player);
            // The GUI is closed automatically after a trade attempt in the execute method.
        } else {
            player.sendMessage(ChatColor.RED + "[Debug] Could not identify the clicked trade. Item might be missing data.");
        }
    }
}
