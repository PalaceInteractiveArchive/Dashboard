package network.palace.dashboard.commands.guide;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuideListCommand extends DashboardCommand {

    public GuideListCommand() {
        super(Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        List<Player> guides = new ArrayList<>();
        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            if (tp.hasTag(RankTag.GUIDE)) {
                guides.add(tp);
            }
        }
        guides.sort(Comparator.comparing(o -> o.getUsername().toLowerCase()));

        ComponentBuilder comp = new ComponentBuilder("Online Guides (" + guides.size() + "): ").color(ChatColor.DARK_GREEN);
        int i = 0;
        for (Player p : guides) {
            comp.append(p.getUsername(), ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Currently on: ")
                            .color(ChatColor.GREEN).append(p.getServer()).color(ChatColor.AQUA).create()));
            if (i < (guides.size() - 1)) comp.append(", ");
            i++;
        }
        player.sendMessage(comp.create());
    }
}
