package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.UUID;

public class Commandunmute extends MagicCommand {

    public Commandunmute() {
        super(Rank.KNIGHT);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unmute [Username]");
            return;
        }
        String username = args[0];
        Player tp = dashboard.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = dashboard.getSqlUtil().uuidFromUsername(username);
        } else {
            uuid = tp.getUniqueId();
            username = tp.getName();
        }
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        dashboard.getSqlUtil().unmutePlayer(uuid);
        tp.getMute().setMuted(false);
        dashboard.getModerationUtil().announceUnmute(username, player.getName());
    }
}