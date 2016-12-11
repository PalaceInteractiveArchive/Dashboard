package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommandMentions extends MagicCommand {

    @Override
    public void execute(final Player player, String label, String[] args) {
        player.setMentions(!player.isMentions());
        player.sendMessage((player.isMentions() ? ChatColor.GREEN : ChatColor.RED) + "You have " +
                (player.isMentions() ? "enabled" : "disabled") + " mention notifications!");

        if (player.isMentions()) {
            player.mention();
        }

        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET mentions=? WHERE uuid=?");

                sql.setInt(1, player.isMentions() ? 1 : 0);
                sql.setString(2, player.getUuid().toString());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}