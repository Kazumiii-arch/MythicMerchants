package com.mythic.merchants.commands;

import com.mythic.merchants.MythicMerchants;
import com.mythic.merchants.managers.ReputationManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayerCommands implements CommandExecutor {

    private final MythicMerchants plugin;
    private final ReputationManager reputationManager;

    public PlayerCommands(MythicMerchants plugin) {
        this.plugin = plugin;
        this.reputationManager = plugin.getReputationManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("reputation")) {
            if (!player.hasPermission("mythicmerchants.reputation")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            displayReputation(player);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("merchant")) {
             if (!player.hasPermission("mythicmerchants.trade")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            player.sendMessage(ChatColor.YELLOW + "Right-click a merchant to trade with them!");
            return true;
        }

        return false;
    }

    private void displayReputation(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Your Reputations ---");
        Map<String, Integer> playerRep = reputationManager.getPlayerReputationMap(player.getUniqueId());
        
        for (String faction : plugin.getConfig().getStringList("reputation.factions")) {
            int rep = playerRep.getOrDefault(faction.toUpperCase(), 0);
            String tier = reputationManager.getTier(rep);
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + faction + ": " + ChatColor.WHITE + rep + " (" + tier + ")");
        }
    }
}
