package network.palace.dashboard.commands.admin;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Marc on 9/1/16
 */
public class CmdsCommand extends DashboardCommand {

    public CmdsCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Registered Commands:");
        StringBuilder msg = null;
        TreeMap<String, DashboardCommand> map = Launcher.getDashboard().getCommandUtil().getCommands();
        for (Map.Entry<String, DashboardCommand> entry : map.entrySet()) {
            if (msg != null) {
                msg.append("\n");
            } else {
                msg = new StringBuilder();
            }
            msg.append(ChatColor.YELLOW).append("- /").append(entry.getKey()).append(" ");
            List<String> aliases = entry.getValue().getAliases();
            if (!aliases.isEmpty()) {
                msg.append("(");
                for (int i = 0; i < aliases.size(); i++) {
                    msg.append(aliases.get(i));
                    if (i < (aliases.size() - 1)) {
                        msg.append(", ");
                    }
                }
                msg.append(") ");
            }
            msg.append(entry.getValue().getRank().getTagColor()).append(entry.getValue().getRank().getName());
        }
        player.sendMessage(msg.toString());
    }
}
