package com.mythic.merchants.commands;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReputationCommand implements CommandExecutor {

    public ReputationCommand(MythicMerchants plugin) {
        // Constructor can be used to get manager classes
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }
        Player player = (Player) sender;
        // In a real implementation, you would get this data from the ReputationManager
        player.sendMessage(ChatColor.GOLD + "--- Your Reputations ---");
        player.sendMessage(ChatColor.AQUA + "ARTISANS: " + ChatColor.WHITE + "0 (Neutral)");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "MYSTICS: " + ChatColor.WHITE + "0 (Neutral)");
        player.sendMessage(ChatColor.GREEN + "EXPLORERS: " + ChatColor.WHITE + "0 (Neutral)");
        return true;
    }
}
