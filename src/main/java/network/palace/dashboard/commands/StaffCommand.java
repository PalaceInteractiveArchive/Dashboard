package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketDisablePlayer;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Marc on 4/8/17.
 */
public class StaffCommand extends DashboardCommand {
    private static HashMap<UUID, Integer> attempts = new HashMap<>();

    public StaffCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            if (args.length == 2 && args[0].equalsIgnoreCase("login")) {
                if (!player.isDisabled()) {
                    player.sendMessage(ChatColor.GREEN + "You're already logged in!");
                    return;
                }
                if (dashboard.getMongoHandler().verifyPassword(player.getUniqueId(), args[1])) {
                    player.setDisabled(false);
                    player.sendMessage(ChatColor.GREEN + "You logged in!");
                    dashboard.getMongoHandler().updateStaffIP(player);
                    PacketDisablePlayer packet = new PacketDisablePlayer(player.getUniqueId(), false);
                    Dashboard.getInstance(player.getServer()).send(packet);
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("[Successful] *" + player.getRank().getName() + "* " +
                            player.getUsername() + " " + player.getAddress());
                    a.color("good");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                } else {
                    int trial;
                    if (attempts.containsKey(player.getUniqueId())) {
                        attempts.put(player.getUniqueId(), attempts.remove(player.getUniqueId()) + 1);
                        trial = attempts.get(player.getUniqueId());
                    } else {
                        attempts.put(player.getUniqueId(), 1);
                        trial = 1;
                    }
                    if (trial >= 5) {
                        Ban ban = new Ban(player.getUniqueId(), player.getUsername(), true, System.currentTimeMillis(),
                                "Locked out of staff account", "Dashboard");
                        dashboard.getMongoHandler().banPlayer(ban);
                        dashboard.getModerationUtil().announceBan(ban);
                        player.kickPlayer(ChatColor.RED + "Locked out of staff account. Please contact management to unlock your account.");
                        SlackMessage m = new SlackMessage("<!channel> *" + player.getUsername() + " Locked Out*");
                        SlackAttachment a = new SlackAttachment("*[Locked] " + player.getRank().getName() + "* " +
                                player.getUsername() + " " + player.getAddress());
                        a.color("danger");
                        dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "Incorrect password!");
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("[" + trial + "/5] *" + player.getRank().getName() + "* " +
                            player.getUsername() + " " + player.getAddress());
                    a.color("warning");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                }
                return;
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("change")) {
                    String oldp = args[1];
                    String newp = args[2];
                    if (newp.length() > 256) {
                        player.sendMessage(ChatColor.RED + "Passwords cannot be larger than 256 characters!");
                        return;
                    }
                    if (!dashboard.getPasswordUtil().isStrongEnough(newp)) {
                        player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                        return;
                    }
                    if (!dashboard.getMongoHandler().verifyPassword(player.getUniqueId(), oldp)) {
                        player.sendMessage(ChatColor.RED + "Your existing password is incorrect!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[Failed PW Change] *" + player.getRank().getName() + "* " +
                                player.getUsername() + " " + player.getAddress());
                        a.color("warning");
                        dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                        return;
                    }
                    dashboard.getMongoHandler().changePassword(player.getUniqueId(), newp);
                    player.sendMessage(ChatColor.GREEN + "Your password was successfully changed!");
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("[PW Changed] *" + player.getRank().getName() + "* " +
                            player.getUsername() + " " + player.getAddress());
                    a.color("good");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                    return;
                } else if (args[0].equalsIgnoreCase("force") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                    String username;
                    String pass = args[2];
                    UUID uuid = dashboard.getMongoHandler().usernameToUUID(args[1]);
                    if (uuid == null) {
                        player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                        return;
                    }
                    username = dashboard.getMongoHandler().uuidToUsername(uuid);
                    if (username.equalsIgnoreCase("unknown")) {
                        player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                        return;
                    }
                    if (pass.length() > 256) {
                        player.sendMessage(ChatColor.RED + "Passwords cannot be larger than 256 characters!");
                        return;
                    }
                    if (!dashboard.getPasswordUtil().isStrongEnough(pass)) {
                        player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                        return;
                    }
                    if (dashboard.getMongoHandler().hasPassword(uuid)) {
                        dashboard.getMongoHandler().changePassword(uuid, pass);
                    } else {
                        dashboard.getMongoHandler().setPassword(uuid, pass);
                    }
                    player.sendMessage(ChatColor.GREEN + username + "'s password was successfully changed!");
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("[PW Force-Changed] *" + username +
                            "* changed by *" + player.getUsername() + "* " + player.getAddress());
                    a.color("good");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                    return;
                }
            }
            player.sendMessage(ChatColor.GREEN + "Staff commands:");
            player.sendMessage(ChatColor.GREEN + "/staff login [password] " + ChatColor.YELLOW + "- Login to a staff account");
            player.sendMessage(ChatColor.GREEN + "/staff change [old password] [new password] " +
                    ChatColor.YELLOW + "- Change your staff password");
            if (player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                player.sendMessage(ChatColor.GOLD + "/staff force [Username] [Password] - Force-change a staff member's password");
            }
        });
    }

    public static void logout(UUID uuid) {
        attempts.remove(uuid);
    }
}
