package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
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
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Registered Commands:");
        String msg = null;
        TreeMap<String, MagicCommand> map = Dashboard.commandUtil.getCommands();
        for (Map.Entry<String, MagicCommand> entry : map.entrySet()) {
            if (msg != null) {
                msg += "\n";
            } else {
                msg = "";
            }
            msg += "- /" + entry.getKey() + " ";
            List<String> aliases = entry.getValue().getAliases();
            if (!aliases.isEmpty()) {
                msg += "(";
                for (int i = 0; i < aliases.size(); i++) {
                    msg += aliases.get(i);
                    if (i < (aliases.size() - 1)) {
                        msg += ", ";
                    }
                }
                msg += ") ";
            }
            msg += entry.getValue().getRank().getTagColor() + entry.getValue().getRank().getName();
        }
        player.sendMessage(ChatColor.YELLOW + msg);
    }
}
