package com.mythic.merchants.listeners;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.AdminGUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles all interactions within the plugin's custom GUIs.
 */
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

        String viewTitle = event.getView().getTitle();

        // --- Admin Dashboard Listener ---
        if (viewTitle.equals(AdminGUIManager.ADMIN_DASHBOARD_TITLE)) {
            // Prevent players from taking items out of the admin GUI
            event.setCancelled(true);
            
            // Here you would add a switch statement for event.getSlot()
            // to handle clicks on "Merchant Profiles", "Global Settings", etc.
            // For example:
            // if (event.getSlot() == 10) {
            //     plugin.getAdminGUIManager().openProfileListGUI(player);
            // }
        }

        // --- Player Trade GUI Listener ---
        // You would add another 'else if' block here to listen for clicks
        // in the player-facing trade GUI. This is where you would call
        // the MerchantManager's 'executeTrade' method.
    }
}
