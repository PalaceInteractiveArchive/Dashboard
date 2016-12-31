package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;

import java.util.Collections;

public class CommandFriend extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandFriend() {
        aliases = Collections.singletonList("f");
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        dashboard.getFriendUtil().helpMenu(player);
                        return;
                    case "list":
                        dashboard.getFriendUtil().listFriends(player, 1);
                        return;
                    case "toggle":
                        dashboard.getSchedulerManager().runAsync(() -> {
                            player.setHasFriendToggled(!player.hasFriendToggledOff());

                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.GREEN + "ON");
                            }
                            dashboard.getFriendUtil().toggleRequests(player);
                        });
                        return;
                    case "requests":
                        dashboard.getFriendUtil().listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!isInt(args[1])) {
                            dashboard.getFriendUtil().listFriends(player, 1);
                            return;
                        }

                        dashboard.getFriendUtil().listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = dashboard.getPlayer(user);

                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }

                        if (!player.getFriends().containsKey(tp.getUuid())) {
                            player.sendMessage(ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        dashboard.getFriendUtil().teleportPlayer(player, tp);
                        return;
                    case "add":
                        dashboard.getFriendUtil().addFriend(player, args[1]);
                        return;
                    case "remove":
                        dashboard.getFriendUtil().removeFriend(player, args[1]);
                        return;
                    case "accept":
                        dashboard.getFriendUtil().acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        dashboard.getFriendUtil().denyFriend(player, args[1]);
                        return;
                }
        }
        dashboard.getFriendUtil().helpMenu(player);
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