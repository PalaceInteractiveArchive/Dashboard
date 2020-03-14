package network.palace.dashboard.commands.staff;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.*;

public class StaffListCommand extends DashboardCommand {

    public StaffListCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        TreeMap<Rank, Set<Player>> players = new TreeMap<>(Comparator.comparingInt(Enum::ordinal));
        Launcher.getDashboard().getOnlinePlayers().stream()
                .filter(p -> p.getRank().getRankId() >= Rank.TRAINEE.getRankId())
                .forEach(tp -> {
                    Rank rank = tp.getRank();
                    if (rank.equals(Rank.TRAINEEBUILD) || rank.equals(Rank.TRAINEETECH)) rank = Rank.TRAINEE;
                    Set<Player> list = players.getOrDefault(rank, new TreeSet<>(Comparator.comparing(Player::getUsername)));
                    list.add(tp);
                    if (!players.containsKey(rank)) players.put(rank, list);
                });
        player.sendMessage(ChatColor.GREEN + "Online Staff Members:");
        for (Map.Entry<Rank, Set<Player>> entry : players.entrySet()) {
            sendRankMessage(player, entry.getKey(), entry.getValue());
        }
    }

    private void sendRankMessage(Player player, Rank rank, Set<Player> members) {
        ComponentBuilder comp = new ComponentBuilder(rank.getName() +
                (rank.equals(Rank.MEDIA) ? "" : "s") +
                ": (" + members.size() + ") ").color(rank.getTagColor());
        int i = 0;
        for (Player p : members) {
            comp.append(p.getUsername(), ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Currently on: ")
                            .color(ChatColor.GREEN).append(p.getServer()).color(ChatColor.AQUA).create()));
            if (i < (members.size() - 1)) comp.append(", ");
            i++;
        }
        player.sendMessage(comp.create());
    }
}