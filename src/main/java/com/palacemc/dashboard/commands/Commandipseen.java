package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.AddressBan;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketIPSeenCommand;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;

import java.util.List;

public class Commandipseen extends MagicCommand {

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/ipseen [IP Address]");
            return;
        }
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                AddressBan ban = Dashboard.sqlUtil.getAddressBan(args[0]);
                if (ban != null) {
                    player.sendMessage(ChatColor.RED + "This IP Address is banned for " + ChatColor.AQUA + ban.getReason());
                }
                List<String> users = Dashboard.sqlUtil.getNamesFromIP(args[0]);
                if (users == null || users.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
                    return;
                }
                PacketIPSeenCommand packet = new PacketIPSeenCommand(player.getUniqueId(), users, args[0]);
                player.send(packet);
            }
        });
    }
}