package com.mythic.merchants;

import com.mythic.merchants.commands.AdminCommand;
import com.mythic.merchants.commands.PlayerCommands;
import com.mythic.merchants.listeners.GUIListener;
import com.mythic.merchants.listeners.MerchantListener;
import com.mythic.merchants.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MythicMerchants extends JavaPlugin {

    private static MythicMerchants instance;
    private MerchantManager merchantManager;
    private AdminGUIManager adminGUIManager;
    private GUIManager tradeGUIManager;
    private ReputationManager reputationManager;
    private EconomyManager economyManager;
    private TipManager tipManager;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        // Configuration setup
        saveDefaultConfig();
        File merchantsDir = new File(getDataFolder(), "merchants");
        if (!merchantsDir.exists()) merchantsDir.mkdirs();
        saveResource("merchants/banker.yml", false);
        saveResource("merchants/wandering_wizard.yml", false);
        File dataDir = new File(getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();

        // Service setup
        setupEconomy();

        // Manager initialization
        this.reputationManager = new ReputationManager(this);
        this.economyManager = new EconomyManager(this);
        this.merchantManager = new MerchantManager(this);
        this.adminGUIManager = new AdminGUIManager(this);
        this.tradeGUIManager = new GUIManager(this);
        this.tipManager = new TipManager(this);

        // Command registration
        getCommand("merchantadmin").setExecutor(new AdminCommand(this));
        PlayerCommands playerCommands = new PlayerCommands(this);
        getCommand("reputation").setExecutor(playerCommands);
        getCommand("merchant").setExecutor(playerCommands);

        // Event listener registration
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new MerchantListener(this), this);

        // Start scheduled tasks
        tipManager.startTipTask();
        merchantManager.startRandomSpawnTask();
        economyManager.startRestockTask();
        
        // Load persistent data after a small delay to ensure all plugins are loaded
        getServer().getScheduler().runTaskLater(this, () -> merchantManager.loadPermanentMerchants(), 20L);

        getLogger().info("MythicMerchants v3.0.0 has been enabled!");
    }

    @Override
    public void onDisable() {
        if(merchantManager != null) merchantManager.despawnAllMerchants(false);
        if(economyManager != null) economyManager.saveData();
        if(reputationManager != null) reputationManager.saveData();
        getLogger().info("MythicMerchants has been disabled.");
    }
    
    public void reloadPlugin() {
        reloadConfig();
        tipManager.reloadAndRestart();
        merchantManager.reloadAllProfiles();
        economyManager.reload();
        reputationManager.reload();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    // --- Getters ---
    public static MythicMerchants getInstance() { return instance; }
    public MerchantManager getMerchantManager() { return merchantManager; }
    public AdminGUIManager getAdminGUIManager() { return adminGUIManager; }
    public GUIManager getTradeGUIManager() { return tradeGUIManager; }
    public ReputationManager getReputationManager() { return reputationManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public static Economy getEconomy() { return econ; }
}
