package com.mythic.merchants.commands;

import com.mythic.merchants.MythicMerchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final MythicMerchants plugin;

    public AdminCommand(MythicMerchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("mythicmerchants.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to access the admin dashboard.");
            return true;
        }
        plugin.getAdminGUIManager().openAdminDashboard(player);
        return true;
    }
  }
