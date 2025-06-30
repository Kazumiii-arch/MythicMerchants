package com.mythic.merchants;

import com.mythic.merchants.commands.AdminCommand;
import com.mythic.merchants.commands.ReputationCommand;
import com.mythic.merchants.listeners.GUIListener;
import com.mythic.merchants.listeners.MerchantListener;
import com.mythic.merchants.managers.AdminGUIManager;
import com.mythic.merchants.managers.MerchantManager;
import com.mythic.merchants.managers.TipManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicMerchants extends JavaPlugin {

    private static MythicMerchants instance;
    private MerchantManager merchantManager;
    private AdminGUIManager adminGUIManager;
    private TipManager tipManager;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        saveResource("merchants/banker.yml", false);
        saveResource("merchants/wandering_wizard.yml", false);

        setupEconomy();

        this.merchantManager = new MerchantManager(this);
        this.adminGUIManager = new AdminGUIManager(this);
        this.tipManager = new TipManager(this);

        getCommand("merchantadmin").setExecutor(new AdminCommand(this));
        getCommand("reputation").setExecutor(new ReputationCommand(this));
        
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new MerchantListener(this), this);

        tipManager.startTipTask();
        // The merchant spawn task would be started inside the MerchantManager's constructor or another method.

        getLogger().info("MythicMerchants v3.0.0 has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static MythicMerchants getInstance() {
        return instance;
    }

    public MerchantManager getMerchantManager() {
        return merchantManager;
    }

    public AdminGUIManager getAdminGUIManager() {
        return adminGUIManager;
    }
    
    public static Economy getEconomy() {
        return econ;
    }
}
