package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommandMentions extends MagicCommand {

    @Override
    public void execute(final Player player, String label, String[] args) {
        player.setMentions(!player.hasMentions());
        player.sendMessage((player.hasMentions() ? ChatColor.GREEN : ChatColor.RED) + "You have " +
                (player.hasMentions() ? "enabled" : "disabled") + " mention notifications!");
        if (player.hasMentions()) {
            player.mention();
        }
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                    PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET mentions=? WHERE uuid=?");
                    sql.setInt(1, player.hasMentions() ? 1 : 0);
                    sql.setString(2, player.getUniqueId().toString());
                    sql.execute();
                    sql.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}