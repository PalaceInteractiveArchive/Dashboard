package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.utils.FriendUtil;

import java.util.Arrays;

public class FriendCommand extends DashboardCommand {

    public FriendCommand() {
        aliases = Arrays.asList("f");
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        FriendUtil.helpMenu(player);
                        return;
                    case "list":
                        FriendUtil.listFriends(player, 1);
                        return;
                    case "toggle":
                        dashboard.getSchedulerManager().runAsync(() -> {
                            player.setToggled(!player.hasFriendToggledOff());
                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " +
                                        ChatColor.GREEN + "ON");
                            }
                            FriendUtil.toggleRequests(player);
                        });
                        return;
                    case "requests":
                        FriendUtil.listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!isInt(args[1])) {
                            FriendUtil.listFriends(player, 1);
                            return;
                        }
                        FriendUtil.listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = dashboard.getPlayer(user);
                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        FriendUtil.teleportPlayer(player, tp);
                        return;
                    case "add":
                        FriendUtil.addFriend(player, args[1]);
                        return;
                    case "remove":
                        FriendUtil.removeFriend(player, args[1]);
                        return;
                    case "accept":
                        FriendUtil.acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        FriendUtil.denyFriend(player, args[1]);
                        return;
                }
        }
        FriendUtil.helpMenu(player);
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