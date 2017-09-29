package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Marc on 9/1/16
 */
public class Commandcmds extends MagicCommand {

    public Commandcmds() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Registered Commands:");
        StringBuilder msg = null;
        TreeMap<String, MagicCommand> map = Launcher.getDashboard().getCommandUtil().getCommands();
        for (Map.Entry<String, MagicCommand> entry : map.entrySet()) {
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
