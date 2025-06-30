package com.mythic.merchants.objects;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.EconomyManager;
import com.mythic.merchants.managers.ItemManager;
import com.mythic.merchants.managers.ReputationManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MerchantTrade {

    private final MythicMerchants plugin;
    private final String tradeId;
    private final List<String> costStrings;
    private final ItemStack resultItem;
    private final int stock;
    private final String reputationRequired;
    private final String faction;

    public MerchantTrade(MythicMerchants plugin, String tradeId, ConfigurationSection cs, String faction) throws IOException {
        this.plugin = plugin;
        this.tradeId = tradeId;
        this.costStrings = cs.getStringList("cost");
        this.resultItem = ItemManager.deserialize(cs.getString("result-item"));
        this.stock = cs.getInt("stock", -1);
        this.reputationRequired = cs.getString("reputation-required");
        this.faction = faction;
    }

    public ItemStack getDisplayItem(Player player) {
        ReputationManager repManager = plugin.getReputationManager();
        EconomyManager econManager = plugin.getEconomyManager();
        
        ItemStack display = resultItem.clone();
        ItemMeta meta = display.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Cost:");

        for(String costStr : costStrings) {
            String[] parts = costStr.split(":");
            String type = parts[0].toUpperCase();
            double amount = Double.parseDouble(parts[1]);
            
            if(type.equals("VAULT")) {
                 lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GREEN + "$" + String.format("%,.2f", amount));
            } else if (type.equals("CUSTOM_CURRENCY")) {
                lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + (int)amount + "x " + "Gold Coins"); // Example name
            } else {
                 lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + (int)amount + "x " + type.replace("_", " "));
            }
        }
        
        lore.add(" ");
        
        boolean canAfford = hasAllCosts(player);
        boolean hasRep = repManager.hasRequiredReputation(player, faction, reputationRequired);
        
        if(!hasRep) canAfford = false;
        lore.add(hasRep ? ChatColor.GREEN + "✔ Reputation" : ChatColor.RED + "✖ Requires " + reputationRequired + " Reputation");
        
        int currentStock = econManager.getGlobalStock(tradeId);
        if(stock != -1 && currentStock <= 0) canAfford = false;
        if(stock != -1) {
            lore.add(currentStock > 0 ? ChatColor.GREEN + "✔ In Stock (" + currentStock + ")" : ChatColor.RED + "✖ Out of Stock");
        }
        
        lore.add(" ");
        lore.add(canAfford ? ChatColor.YELLOW + "Click to purchase!" : ChatColor.DARK_RED + "Cannot purchase.");
        
        meta.setLore(lore);
        
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "trade_id"), PersistentDataType.STRING, tradeId);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "profile_id"), PersistentDataType.STRING, "placeholder_profile"); // A better system would pass the profileId in
        display.setItemMeta(meta);
        
        return display;
    }
    
    public void execute(Player player) {
        if (!hasAllCosts(player)) {
            player.sendMessage(ChatColor.RED + "You do not meet the requirements for this trade!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Take costs
        for (String costStr : costStrings) {
            String[] parts = costStr.split(":");
            String type = parts[0].toUpperCase();
            double amount = Double.parseDouble(parts[1]);
            
            if (type.equals("VAULT")) {
                Economy econ = MythicMerchants.getEconomy();
                econ.withdrawPlayer(player, amount);
            } else {
                // Logic to remove physical items (custom currency or regular items)
                player.getInventory().removeItem(new ItemStack(Material.valueOf(type), (int)amount));
            }
        }

        // Give result item
        player.getInventory().addItem(resultItem.clone());
        player.sendMessage(ChatColor.GREEN + "Trade successful!");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        
        // Update managers
        plugin.getReputationManager().addReputation(player.getUniqueId(), faction, plugin.getConfig().getInt("reputation.points-per-trade", 1));
        plugin.getEconomyManager().decrementGlobalStock(tradeId);
        plugin.getEconomyManager().updatePriceOnBuy(resultItem.getType().name()); // Simplified
        
        player.closeInventory();
    }
    
    private boolean hasAllCosts(Player player) {
        // Full cost checking logic would go here, checking inventory and Vault balance
        return true; // Placeholder
    }

    public static MerchantTrade fromDisplayItem(MythicMerchants plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        NamespacedKey key = new NamespacedKey(plugin, "trade_id");
        String tradeId = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(tradeId == null) return null;

        // This is a simplified retrieval. A better system would find which profile this item came from.
        String tempProfileId = "wandering_wizard"; // Fallback
        ConfigurationSection cs = plugin.getMerchantManager().getProfileConfig(tempProfileId).getConfigurationSection("trade-pool." + tradeId);
        String faction = plugin.getMerchantManager().getProfileConfig(tempProfileId).getString("settings.faction");
        
        try {
            return new MerchantTrade(plugin, tradeId, cs, faction);
        } catch (IOException e) {
            return null;
        }
    }
}
