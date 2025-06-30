package com.mythic.merchants.listeners;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.GUIManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handles direct player interactions with merchant entities in the world.
 */
public class MerchantListener implements Listener {

    private final MythicMerchants plugin;
    // GUIManager would be the one that handles player-facing trade GUIs
    private final GUIManager tradeGUIManager;

    public MerchantListener(MythicMerchants plugin) {
        this.plugin = plugin;
        // In a full implementation, you'd initialize a new GUIManager here
        this.tradeGUIManager = new GUIManager(plugin); 
    }

    @EventHandler
    public void onMerchantInteract(PlayerInteractEntityEvent event) {
        // Ensure the interaction is a right-click
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();
        Player player = event.getPlayer();

        // Check if the clicked entity is a merchant managed by this plugin.
        // This requires a method in MerchantManager to check if an entity's UUID
        // is an active merchant, e.g., plugin.getMerchantManager().isMerchant(clickedEntity.getUniqueId())
        
        // --- Placeholder Logic ---
        // if (plugin.getMerchantManager().isMerchant(clickedEntity.getUniqueId())) {
        //     // Prevent the default villager GUI from opening
        //     event.setCancelled(true);
            
        //     String profileId = plugin.getMerchantManager().getProfileId(clickedEntity.getUniqueId());
            
        //     // Open the custom trade GUI for the player
        //     tradeGUIManager.openTradeGUI(player, profileId);
        // }
    }
}
