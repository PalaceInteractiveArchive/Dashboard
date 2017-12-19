package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartiesCommand extends DashboardCommand {

    public PartiesCommand() {
        super(Rank.TRAINEE);
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
            StringBuilder msg = null;
            for (Party p : parties) {
                String leader = p.getLeader().getUsername();
                if (msg != null) {
                    msg.append("\n");
                } else {
                    msg = new StringBuilder();
                }
                msg.append("- ").append(leader).append(" ").append(p.getMembers().size()).append(" Member").append(p.getMembers().size() > 1 ? "s" : "");
            }
            player.sendMessage(ChatColor.GREEN + msg.toString());
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
            Player pl = dashboard.getPlayer(uuid);
            if (pl == null) {
                continue;
            }
            names.add(pl.getUsername());
        }
        StringBuilder msg = new StringBuilder("Party Leader: " + p.getLeader().getUsername() + "\nParty Members: ");
        for (int i = 0; i < names.size(); i++) {
            msg.append(names.get(i));
            if (i < (names.size() - 1)) {
                msg.append(", ");
            }
        }
        player.sendMessage(ChatColor.YELLOW + msg.toString());
    }
}