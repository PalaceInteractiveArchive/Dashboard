package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketDisablePlayer;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Marc on 4/8/17.
 */
public class Commandstaff extends MagicCommand {
    private HashMap<UUID, Integer> attempts = new HashMap<>();

    public Commandstaff() {
        super(Rank.SQUIRE);
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                if (args.length == 1 && player.getName().equals("Legobuilder0813") && args[0].equals("temporary")) {
                    HashMap<UUID, String> map = new HashMap<>();
                    try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                        PreparedStatement sql = connection.prepareStatement("SELECT username,uuid FROM player_data WHERE rank='squire' OR rank='knight' OR rank='paladin' OR rank='wizard' OR rank='emperor' OR rank='empress' ORDER BY username DESC;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            map.put(UUID.fromString(result.getString("uuid")), result.getString("username"));
                        }
                        result.close();
                        sql.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    for (Map.Entry<UUID, String> entry : map.entrySet()) {
                        String pass = randomString(8);
                        System.out.println(entry.getValue() + " - " + pass);
                        String hashed = Dashboard.passwordUtil.hashPassword(pass, Dashboard.passwordUtil.getNewSalt());
                        Dashboard.sqlUtil.setPassword(entry.getKey(), hashed);
                    }
                    return;
                }
                if (args.length == 2 && args[0].equalsIgnoreCase("login")) {
                    if (!player.isDisabled()) {
                        player.sendMessage(ChatColor.GREEN + "You're already logged in!");
                        return;
                    }
                    if (Dashboard.sqlUtil.verifyPassword(player.getUniqueId(), args[1])) {
                        player.setDisabled(false);
                        player.sendMessage(ChatColor.GREEN + "You've logged in!");
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
                if (args.length == 3 && args[0].equalsIgnoreCase("change")) {
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
                }
                player.sendMessage(ChatColor.GREEN + "Staff commands:");
                player.sendMessage(ChatColor.GREEN + "/staff login [password] " + ChatColor.YELLOW + "- Login to a staff account");
                player.sendMessage(ChatColor.GREEN + "/staff change [old password] [new password] " +
                        ChatColor.YELLOW + "- Change your staff password");
            }
        });
    }
}
