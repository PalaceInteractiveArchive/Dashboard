package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Commandparties extends MagicCommand {

    public Commandparties() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length != 2 || !args[0].equalsIgnoreCase("info")) {
            List<Party> parties = dashboard.getPartyUtil().getParties();
            if (parties.isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no Parties right now!");
                return;
            }
            player.sendMessage(ChatColor.YELLOW + "Server Parties:");
            String msg = null;
            for (Party p : parties) {
                String leader = p.getLeader().getUsername();
                if (msg != null) {
                    msg += "\n";
                } else {
                    msg = "";
                }
                msg += "- " + leader + " " + p.getMembers().size() + " Member" + (p.getMembers().size() > 1 ? "s" : "");
            }
            player.sendMessage(ChatColor.GREEN + msg);
            player.sendMessage(ChatColor.YELLOW + "/parties info [Party Leader] " + ChatColor.GREEN + "- Display info on that Party");
            return;
        }
        Player tp = dashboard.getPlayer(args[1]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Party p = dashboard.getPartyUtil().findPartyForPlayer(tp.getUniqueId());
        if (p == null) {
            player.sendMessage(ChatColor.RED + "This player is not in a Party!");
            return;
        }
        List<UUID> members = p.getMembers();
        List<String> names = new ArrayList<>();
        for (UUID uuid : members) {
            names.add(dashboard.getPlayer(uuid).getUsername());
        }
        String msg = "Party Leader: " + p.getLeader().getUsername() + "\nParty Members: ";
        for (int i = 0; i < names.size(); i++) {
            msg += names.get(i);
            if (i < (names.size() - 1)) {
                msg += ", ";
            }
        }
        player.sendMessage(ChatColor.YELLOW + msg);
    }
}