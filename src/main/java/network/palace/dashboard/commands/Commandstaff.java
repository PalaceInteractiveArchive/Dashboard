package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketDisablePlayer;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Marc on 4/8/17.
 */
public class Commandstaff extends MagicCommand {
    private static HashMap<UUID, Integer> attempts = new HashMap<>();

    public Commandstaff() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                if (args.length == 2 && args[0].equalsIgnoreCase("login")) {
                    if (!player.isDisabled()) {
                        player.sendMessage(ChatColor.GREEN + "You're already logged in!");
                        return;
                    }
                    if (Dashboard.sqlUtil.verifyPassword(player.getUniqueId(), args[1])) {
                        player.setDisabled(false);
                        player.sendMessage(ChatColor.GREEN + "You logged in!");
                        Dashboard.sqlUtil.updateStaffIP(player);
                        PacketDisablePlayer packet = new PacketDisablePlayer(player.getUniqueId(), false);
                        Dashboard.getInstance(player.getServer()).send(packet);
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[Successful] *" + player.getRank().getName() + "* " +
                                player.getName() + " " + player.getAddress());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
                    } else {
                        int trial = 0;
                        if (attempts.containsKey(player.getUniqueId())) {
                            attempts.put(player.getUniqueId(), attempts.remove(player.getUniqueId()) + 1);
                            trial = attempts.get(player.getUniqueId());
                        } else {
                            attempts.put(player.getUniqueId(), 1);
                            trial = 1;
                        }
                        if (trial >= 5) {
                            Ban ban = new Ban(player.getUniqueId(), player.getName(), true, System.currentTimeMillis(),
                                    "Locked out of staff account", "Dashboard");
                            Dashboard.sqlUtil.banPlayer(ban);
                            Dashboard.moderationUtil.announceBan(ban);
                            player.kickPlayer(ChatColor.RED + "Locked out of staff account. Please contact management to unlock your account.");
                            SlackMessage m = new SlackMessage("<!channel> *" + player.getName() + " Locked Out*");
                            SlackAttachment a = new SlackAttachment("*[Locked] " + player.getRank().getName() + "* " +
                                    player.getName() + " " + player.getAddress());
                            a.color("danger");
                            Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
                            return;
                        }
                        player.sendMessage(ChatColor.RED + "Incorrect password!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[" + trial + "/5] *" + player.getRank().getName() + "* " +
                                player.getName() + " " + player.getAddress());
                        a.color("warning");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
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
                        if (!Dashboard.passwordUtil.isStrongEnough(newp)) {
                            player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                            return;
                        }
                        if (!Dashboard.sqlUtil.verifyPassword(player.getUniqueId(), oldp)) {
                            player.sendMessage(ChatColor.RED + "Your existing password is incorrect!");
                            SlackMessage m = new SlackMessage("");
                            SlackAttachment a = new SlackAttachment("[Failed PW Change] *" + player.getRank().getName() + "* " +
                                    player.getName() + " " + player.getAddress());
                            a.color("warning");
                            Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
                            return;
                        }
                        Dashboard.sqlUtil.changePassword(player.getUniqueId(), newp);
                        player.sendMessage(ChatColor.GREEN + "Your password was successfully changed!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[PW Changed] *" + player.getRank().getName() + "* " +
                                player.getName() + " " + player.getAddress());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
                        return;
                    } else if (args[0].equalsIgnoreCase("force") && player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                        String username = args[1];
                        String pass = args[2];
                        UUID uuid = Dashboard.sqlUtil.uuidFromUsername(args[1]);
                        if (uuid == null) {
                            player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                    ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                            return;
                        }
                        username = Dashboard.sqlUtil.usernameFromUUID(uuid);
                        if (username.equalsIgnoreCase("unknown")) {
                            player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                    ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                            return;
                        }
                        if (pass.length() > 256) {
                            player.sendMessage(ChatColor.RED + "Passwords cannot be larger than 256 characters!");
                            return;
                        }
                        if (!Dashboard.passwordUtil.isStrongEnough(pass)) {
                            player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                            return;
                        }
                        if (Dashboard.sqlUtil.hasPassword(uuid)) {
                            Dashboard.sqlUtil.changePassword(uuid, pass);
                        } else {
                            Dashboard.sqlUtil.setPassword(uuid, pass);
                        }
                        player.sendMessage(ChatColor.GREEN + username + "'s password was successfully changed!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[PW Force-Changed] *" + username +
                                "* changed by *" + player.getName() + "* " + player.getAddress());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a), false);
                        return;
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Staff commands:");
                player.sendMessage(ChatColor.GREEN + "/staff login [password] " + ChatColor.YELLOW + "- Login to a staff account");
                player.sendMessage(ChatColor.GREEN + "/staff change [old password] [new password] " +
                        ChatColor.YELLOW + "- Change your staff password");
                if (player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                    player.sendMessage(ChatColor.GOLD + "/staff force [Username] [Password] - Force-change a staff member's password");
                }
            }
        });
    }

    public static void logout(UUID uuid) {
        attempts.remove(uuid);
    }
}
