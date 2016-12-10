package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;

import java.util.Arrays;

public class CommandFriend extends MagicCommand {

    public CommandFriend() {
        aliases = Arrays.asList("f");
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        Launcher.getDashboard().getFriendUtil().helpMenu(player);
                        return;
                    case "list":
                        Launcher.getDashboard().getFriendUtil().listFriends(player, 1);
                        return;
                    case "toggle":
                        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
                            player.setHasFriendToggled(!player.hasFriendToggledOff());

                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.GREEN + "ON");
                            }
                            Launcher.getDashboard().getFriendUtil().toggleRequests(player);
                        });
                        return;
                    case "requests":
                        Launcher.getDashboard().getFriendUtil().listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!isInt(args[1])) {
                            Launcher.getDashboard().getFriendUtil().listFriends(player, 1);
                            return;
                        }

                        Launcher.getDashboard().getFriendUtil().listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = Launcher.getDashboard().getPlayer(user);

                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }

                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + tp.getName() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        Launcher.getDashboard().getFriendUtil().teleportPlayer(player, tp);
                        return;
                    case "add":
                        Launcher.getDashboard().getFriendUtil().addFriend(player, args[1]);
                        return;
                    case "remove":
                        Launcher.getDashboard().getFriendUtil().removeFriend(player, args[1]);
                        return;
                    case "accept":
                        Launcher.getDashboard().getFriendUtil().acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        Launcher.getDashboard().getFriendUtil().denyFriend(player, args[1]);
                        return;
                }
        }
        Launcher.getDashboard().getFriendUtil().helpMenu(player);
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}