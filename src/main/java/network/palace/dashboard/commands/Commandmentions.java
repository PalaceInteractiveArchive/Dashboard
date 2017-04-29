package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Commandmentions extends MagicCommand {

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        player.setMentions(!player.hasMentions());
        player.sendMessage((player.hasMentions() ? ChatColor.GREEN : ChatColor.RED) + "You have " +
                (player.hasMentions() ? "enabled" : "disabled") + " mention notifications!");
        if (player.hasMentions()) {
            player.mention();
        }
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = dashboard.getSqlUtil().getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET mentions=? WHERE uuid=?");
                sql.setInt(1, player.hasMentions() ? 1 : 0);
                sql.setString(2, player.getUniqueId().toString());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}