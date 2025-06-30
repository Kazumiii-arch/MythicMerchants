package com.mythic.merchants.managers;

import com.mythic.merchants.MythicMerchants;
// This class would be very large. This is a placeholder structure.
public class MerchantManager {

    private final MythicMerchants plugin;
    private final ReputationManager reputationManager;
    private final EconomyManager economyManager;

    public MerchantManager(MythicMerchants plugin) {
        this.plugin = plugin;
        this.reputationManager = new ReputationManager(plugin);
        this.economyManager = new EconomyManager(plugin);
        // Logic to load all merchant profiles from the /merchants folder would go here.
    }
    
    // Placeholder method for starting the random spawn task
    public void startRandomSpawnTask() {
        // This would contain the BukkitRunnable to try spawning merchants.
    }
}
