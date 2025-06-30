package com.mythic.merchants.listeners;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.AdminGUIManager;
import com.mythic.merchants.managers.GUIManager;
import com.mythic.merchants.objects.MerchantTrade;
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
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        String viewTitle = event.getView().getTitle();

        if (viewTitle.equals(AdminGUIManager.ADMIN_DASHBOARD_TITLE)) {
            event.setCancelled(true);
            return;
        }

        if (viewTitle.startsWith(GUIManager.TRADE_GUI_TITLE_PREFIX)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            MerchantTrade trade = MerchantTrade.fromDisplayItem(plugin, clickedItem);

            if (trade != null) {
                trade.execute(player);
            }
        }
    }
}
