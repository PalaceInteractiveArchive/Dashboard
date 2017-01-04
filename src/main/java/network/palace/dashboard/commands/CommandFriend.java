package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;

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
                        Dashboard.friendUtil.helpMenu(player);
                        return;
                    case "list":
                        Dashboard.friendUtil.listFriends(player, 1);
                        return;
                    case "toggle":
                        Dashboard.schedulerManager.runAsync(() -> {
                            player.setHasFriendToggled(!player.hasFriendToggledOff());
                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.GREEN + "ON");
                            }
                            Dashboard.friendUtil.toggleRequests(player);
                        });
                        return;
                    case "requests":
                        Dashboard.friendUtil.listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!isInt(args[1])) {
                            Dashboard.friendUtil.listFriends(player, 1);
                            return;
                        }
                        Dashboard.friendUtil.listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = Dashboard.getPlayer(user);
                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + tp.getName() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        Dashboard.friendUtil.teleportPlayer(player, tp);
                        return;
                    case "add":
                        Dashboard.friendUtil.addFriend(player, args[1]);
                        return;
                    case "remove":
                        Dashboard.friendUtil.removeFriend(player, args[1]);
                        return;
                    case "accept":
                        Dashboard.friendUtil.acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        Dashboard.friendUtil.denyFriend(player, args[1]);
                        return;
                }
        }
        Dashboard.friendUtil.helpMenu(player);
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