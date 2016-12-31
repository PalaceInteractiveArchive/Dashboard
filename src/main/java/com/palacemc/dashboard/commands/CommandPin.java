package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketMyMCMagicRegister;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marc on 9/25/16
 */
public class CommandPin extends MagicCommand {
    private Random r = new Random();
    private List<UUID> generating = new ArrayList<>();

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandPin() {
        aliases = Collections.singletonList("mymcmagic");
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        if (generating.contains(player.getUuid())) {
            player.sendMessage(ChatColor.RED + "We're already generating your PIN!");
            return;
        }

        Rank r = player.getRank();
        long curt = System.currentTimeMillis() / 1000;
        // Is between Shareholder and EME, before 10/24 @ 3pm
        if ((r.getRankId() < Rank.SQUIRE.getRankId()) && (r.getRankId() >= Rank.SHAREHOLDER.getRankId())
                && curt < 1477335600) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Shareholders " + ChatColor.GREEN +
                    "will get access to MyMCMagic " + ChatColor.YELLOW + "Monday, October 24th at 3:00pm EST.");
            return;
        }

        // Is DVC, before 10/26 @ 3pm
        if (r.equals(Rank.DVCMEMBER) && curt < 1477508400) {
            player.sendMessage(ChatColor.AQUA + "DVC Members " + ChatColor.GREEN +
                    "will get access to MyMCMagic " + ChatColor.YELLOW + "Wednesday, October 26th at 3:00pm EST.");
            return;
        }

        // Is Guest, before 11/1 @ 3pm
        if (r.equals(Rank.SETTLER) && curt < 1478026800) {
            player.sendMessage(ChatColor.GREEN +
                    "MyMCMagic will release to the public " + ChatColor.YELLOW + "Tuesday, November 1st at 3:00pm EST.");
            player.sendMessage(ChatColor.GREEN + "Purchase " + Rank.DVCMEMBER.getNameWithBrackets() + ChatColor.GREEN +
                    " or " + Rank.SHAREHOLDER.getNameWithBrackets() + ChatColor.GREEN + " for early access! " +
                    ChatColor.YELLOW + "https://store.mcmagic.us");
            return;
        }

        // Is after 10/28 @ 10pm, before 11/1 @ 3pm
        if (curt > 1477706400 && curt < 1478026800) {
            player.sendMessage(ChatColor.GREEN + "We've closed the beta for MyMCMagic until the release on " +
                    ChatColor.YELLOW + "Tuesday, November 1st at 3:00pm EST.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Generating your PIN for MyMCMagic...");
        generating.add(player.getUuid());

        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = dashboard.getActivityUtil().getConnection()) {
                PreparedStatement account = connection.prepareStatement("SELECT id FROM users WHERE uuid=?");

                account.setString(1, player.getUuid().toString());

                ResultSet hasAccount = account.executeQuery();
                boolean has = hasAccount.next();

                hasAccount.close();
                account.close();

                if (has) {
                    player.sendMessage(ChatColor.GREEN + "It looks like you've already registered with " +
                            ChatColor.AQUA + "MyMCMagic! " + ChatColor.GREEN + "Did you lose access to it? Try the " +
                            ChatColor.AQUA + "Forgot Password? " + ChatColor.GREEN + "option next to the " +
                            ChatColor.BLUE + "Sign In " + ChatColor.GREEN + "button.");
                    player.sendMessage(ChatColor.GREEN + "Contact a Cast Member on the server if you need further assistance.");

                    generating.remove(player.getUuid());
                    return;
                }

                PreparedStatement exist = connection.prepareStatement("SELECT pin FROM pins WHERE uuid=?");

                exist.setString(1, player.getUuid().toString());

                ResultSet doesExist = exist.executeQuery();
                boolean exists = doesExist.next();

                if (exists) {
                    int pin = doesExist.getInt("pin");

                    doesExist.close();
                    exist.close();

                    player.sendMessage(ChatColor.GREEN + "Your PIN has already been generated! It is " +
                            ChatColor.AQUA + pin);

                    PacketMyMCMagicRegister packet = new PacketMyMCMagicRegister(player.getUuid(), pin);
                    player.send(packet);

                    generating.remove(player.getUuid());
                    return;
                }

                doesExist.close();
                exist.close();

                int pin = generatePIN();
                int tries = 0;

                while (doesPinExist(connection, pin)) {
                    tries++;
                    if (tries >= 10) {
                        player.sendMessage(ChatColor.GREEN + "We've tried to generate a PIN 10 times but all were taken. Try again soon!");
                        generating.remove(player.getUuid());
                        return;
                    }
                    pin = generatePIN();
                }

                PreparedStatement add = connection.prepareStatement("INSERT INTO pins (uuid,pin) VALUES (?,?)");

                add.setString(1, player.getUuid().toString());
                add.setInt(2, pin);

                add.execute();
                add.close();

                player.sendMessage(ChatColor.GREEN + "Your PIN has been generated! It is " +
                        ChatColor.AQUA + pin);

                PacketMyMCMagicRegister packet = new PacketMyMCMagicRegister(player.getUuid(), pin);
                player.send(packet);

                generating.remove(player.getUuid());
            } catch (SQLException e) {
                e.printStackTrace();

                player.sendMessage(ChatColor.RED + "There was an error generating your PIN! Please try again soon.");

                generating.remove(player.getUuid());
            }
        });
    }

    private boolean doesPinExist(Connection connection, int pin) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("SELECT pin FROM pins WHERE pin=?");

        sql.setInt(1, pin);

        ResultSet result = sql.executeQuery();

        if (result.next() && result.getInt("pin") == pin) {
            result.close();
            sql.close();
            return true;
        }

        result.close();
        sql.close();
        return false;
    }

    private int generatePIN() {
        return r.nextInt(88888889) + 11111111;
    }
}